if redis.call('get',KEYS[1]) == ARGV[1] then
    local count = redis.call('decr',KEYS[2])
    if count and count <= 0  then
        redis.call('del',KEYS[1])
        redis.call('del',KEYS[2])
        return 1
    else
        return 1
    end
else
    return 0
end
