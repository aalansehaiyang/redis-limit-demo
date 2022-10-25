local value = redis.call('get',KEYS[1])

if  not value  then
    redis.call('set',KEYS[1],ARGV[1])
    redis.call('expire',KEYS[1],ARGV[2])
    redis.call('incr',KEYS[2])
    redis.call('expire',KEYS[2],ARGV[2])
    return 1
elseif  value == ARGV[1]  then
    redis.call('incr',KEYS[2])
    return 1
else
    return 0
end
