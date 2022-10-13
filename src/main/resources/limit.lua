local count
count = redis.call('get',KEYS[1])

if count and  tonumber(count) >= tonumber(ARGV[1]) then
    return 0;
end
    count = redis.call('incr',KEYS[1])

if tonumber(count) == 1 then
    redis.call('expire',KEYS[1],ARGV[2])
end

return 1;