package com.daxi.test;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;

/**
 * 纯单元测试辅助工具：在不启动 Spring / 不连数据库的前提下，
 * 为 MyBatis-Plus 实体初始化 {@link TableInfo} 元数据缓存，
 * 使业务代码里的 {@code new LambdaQueryWrapper<X>().eq(X::getXxx, ...)}
 * 能在 mock 掉 mapper 的情况下正常构造（否则会抛“找不到 lambda 缓存”）。
 *
 * <p>仅初始化元数据，不触碰任何数据源，安全可用于单测。
 */
public final class MyBatisPlusTestSupport {

    private MyBatisPlusTestSupport() {
    }

    /**
     * 为指定的实体类初始化 TableInfo 缓存（幂等，重复调用安全）。
     *
     * @param entityClasses 需要初始化的实体类
     */
    public static synchronized void initTableInfo(Class<?>... entityClasses) {
        Configuration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        assistant.setCurrentNamespace("test");
        for (Class<?> clazz : entityClasses) {
            if (TableInfoHelper.getTableInfo(clazz) == null) {
                TableInfoHelper.initTableInfo(assistant, clazz);
            }
        }
    }
}
