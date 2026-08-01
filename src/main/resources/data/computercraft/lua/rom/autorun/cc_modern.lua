
if type(peripheral) == "table" and not peripheral.__ccModernInsidePatched then
    local unpack_fn = table.unpack or unpack
    local function pack_fn(...)
        return { n = select("#", ...), ... }
    end

    local old_get_names = peripheral.getNames
    local old_is_present = peripheral.isPresent
    local old_get_type = peripheral.getType
    local old_has_type = peripheral.hasType
    local old_get_methods = peripheral.getMethods
    local old_call = peripheral.call

    local function configured_hubs()
        local ok, present = pcall(old_is_present, "internal")
        if ok and present then return { "internal" } end
        return {}
    end

    local function hub_ready(hub)
        local ok_present, present = pcall(old_is_present, hub)
        if not ok_present or not present then return false end
        local ok_type, has = pcall(old_has_type, hub, "peripheral_hub")
        return ok_type and has == true
    end

    local function remote_call(hub, method, ...)
        local results = pack_fn(pcall(old_call, hub, method, ...))
        if not results[1] then return nil end
        return unpack_fn(results, 1, results.n)
    end

    local function for_each_hub(fn)
        for _, hub in ipairs(configured_hubs()) do
            if hub_ready(hub) then
                local handled, a, b, c = fn(hub)
                if handled then return true, a, b, c end
            end
        end
        return false, nil
    end

    peripheral.getNames = function()
        local results, seen = {}, {}
        local function add(name)
            if type(name) == "string" and not seen[name] then
                seen[name] = true
                table.insert(results, name)
            end
        end

        for _, name in ipairs(old_get_names()) do add(name) end
        for _, hub in ipairs(configured_hubs()) do
            if hub_ready(hub) then
                add(hub)
                local ok, remote = remote_call(hub, "getNamesRemote")
                if ok and type(remote) == "table" then
                    for _, name in ipairs(remote) do add(name) end
                end
            end
        end
        return results
    end

    peripheral.isPresent = function(name)
        if old_is_present(name) then return true end
        local handled, present = for_each_hub(function(hub)
            local ok, value = remote_call(hub, "isPresentRemote", name)
            if ok and value == true then return true, true end
            return false, nil
        end)
        return handled and present == true
    end

    peripheral.getType = function(name)
        local result = { old_get_type(name) }
        if #result > 0 and result[1] ~= nil then return unpack_fn(result) end

        local handled, values = for_each_hub(function(hub)
            local result_values = { remote_call(hub, "getTypeRemote", name) }
            local ok = table.remove(result_values, 1)
            if ok and #result_values > 0 and result_values[1] ~= nil then return true, result_values end
            return false, nil
        end)
        if handled then return unpack_fn(values) end
        return nil
    end

    peripheral.hasType = function(name, type_name)
        local local_result = { old_has_type(name, type_name) }
        if #local_result > 0 and local_result[1] ~= nil then return local_result[1] end

        local handled, result = for_each_hub(function(hub)
            local ok, value = remote_call(hub, "hasTypeRemote", name, type_name)
            if ok and value ~= nil then return true, value end
            return false, nil
        end)
        if handled then return result end
        return nil
    end

    peripheral.getMethods = function(name)
        local methods = old_get_methods(name)
        if methods ~= nil then return methods end

        local handled, remote = for_each_hub(function(hub)
            local ok, value = remote_call(hub, "getMethodsRemote", name)
            if ok and value ~= nil then return true, value end
            return false, nil
        end)
        if handled then return remote end
        return nil
    end

    peripheral.call = function(name, method, ...)
        local args = pack_fn(...)
        if old_is_present(name) then return old_call(name, method, unpack_fn(args, 1, args.n)) end

        local handled, values, err = for_each_hub(function(hub)
            local result_values = { remote_call(hub, "callRemote", name, method, unpack_fn(args, 1, args.n)) }
            local ok = table.remove(result_values, 1)
            if ok then return true, result_values, nil end
            if result_values[1] ~= nil then return true, nil, result_values[1] end
            return false, nil, nil
        end)
        if handled and values then return unpack_fn(values) end
        if handled and err ~= nil then error(err, 2) end
        return nil
    end

    peripheral.__ccModernInsidePatched = true
end
