package com.oxywire.oxytowns.hooks;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.oxywire.oxytowns.OxyTownsPlugin;

import java.util.function.Consumer;

public class Hooks {
    private static final ClassToInstanceMap<PluginHook> hooks = MutableClassToInstanceMap.create();

    public static <T extends PluginHook> void registerHook(T hook) {
        if (hook.isEnabled()) {
            hooks.putInstance((Class<T>) hook.getClass(), hook);
            OxyTownsPlugin.get().getSLF4JLogger().info("Initialized hook for: {}", hook.getPluginName());
        }
    }

    public static void enableHooks(OxyTownsPlugin plugin) {
        for (final PluginHook hook : hooks.values()) {
            hook.whenEnabled(plugin);
            OxyTownsPlugin.get().getSLF4JLogger().info("Enabled hook for: {}", hook.getPluginName());
        }
    }

    public static <T extends PluginHook> void useHookIfPresent(Class<T> clazz, Consumer<T> consumer) {
        T hook = hooks.getInstance(clazz);
        if (hook != null) {
            consumer.accept(hook);
        }
    }

    public static <T extends PluginHook> void useHookOrElse(Class<T> clazz, Consumer<T> consumer, Runnable elseRunnable) {
        T hook = hooks.getInstance(clazz);
        if (hook != null) {
            consumer.accept(hook);
        } else if (elseRunnable != null) {
            elseRunnable.run();
        }
    }

    public static <T extends PluginHook> T getHook(Class<T> clazz) {
        return hooks.getInstance(clazz);
    }
}
