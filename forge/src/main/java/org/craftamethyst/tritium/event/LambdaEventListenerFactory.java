package org.craftamethyst.tritium.event;

import net.minecraftforge.eventbus.api.Event;
import org.craftamethyst.tritium.Constants;
import org.craftamethyst.tritium.platform.Services;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LambdaEventListenerFactory {
    
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    
    public static IEventListener createStaticListener(Method method) throws Throwable {
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method must be static: " + method);
        }
        
        method.setAccessible(true);
        MethodHandle methodHandle = LOOKUP.unreflect(method);
        MethodType samType = MethodType.methodType(void.class, Event.class);
        
        CallSite callSite = LambdaMetafactory.metafactory(
            LOOKUP,
            "invoke",
            MethodType.methodType(IEventListener.class),
            samType,
            methodHandle,
            methodHandle.type()
        );
        
        return (IEventListener) callSite.getTarget().invokeExact();
    }
    
    public static IEventListener createInstanceListener(Object instance, Method method) throws Throwable {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method must be non-static: " + method);
        }
        
        method.setAccessible(true);
        MethodHandle methodHandle = LOOKUP.unreflect(method);
        MethodHandle boundHandle = methodHandle.bindTo(instance);
        
        MethodType samType = MethodType.methodType(void.class, Event.class);
        
        CallSite callSite = LambdaMetafactory.metafactory(
            LOOKUP,
            "invoke",
            MethodType.methodType(IEventListener.class),
            samType,
            boundHandle,
            boundHandle.type()
        );
        
        return (IEventListener) callSite.getTarget().invokeExact();
    }
    
    public static IEventListener createListener(Object instance, Method method) {
        if (!Services.CONFIG.get().techOptimizations.lambdaEventListeners) {
            return new ReflectionFallbackListener(instance, method);
        }
        
        try {
            if (Modifier.isStatic(method.getModifiers())) {
                return createStaticListener(method);
            } else {
                if (instance == null) {
                    throw new IllegalArgumentException("Instance required for non-static method: " + method);
                }
                return createInstanceListener(instance, method);
            }
        } catch (Throwable t) {
            Constants.LOG.error("Failed to create lambda event listener for method: {}", method, t);
            return new ReflectionFallbackListener(instance, method);
        }
    }
    
    private static class ReflectionFallbackListener implements IEventListener {
        private final Object instance;
        private final Method method;
        
        ReflectionFallbackListener(Object instance, Method method) {
            this.instance = instance;
            this.method = method;
            method.setAccessible(true);
        }
        
        @Override
        public void invoke(Event event) {
            try {
                method.invoke(instance, event);
            } catch (Exception e) {
                Constants.LOG.error("Error invoking event listener: {}", method, e);
            }
        }
    }
}
