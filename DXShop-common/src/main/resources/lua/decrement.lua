local key = KEYS[1]
local quantity = tonumber(ARGV[1])

-- 获取当前库存
local currentStock = tonumber(redis.call('GET', key))

-- 校验：如果 key 不存在或库存不足
if currentStock == nil or currentStock < quantity then
    return -1
end

-- 执行扣减
redis.call('DECRBY', key, quantity)

-- 返回剩余库存
return 0