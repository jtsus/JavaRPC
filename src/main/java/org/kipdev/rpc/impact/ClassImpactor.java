package org.kipdev.rpc.impact;

import javassist.*;
import lombok.SneakyThrows;
import org.kipdev.rpc.Exchange;
import org.kipdev.rpc.RPC;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

public class ClassImpactor {
    private static final String SOURCE_FORMAT = "public void %s(%s) {sendMessage(\"%1$s\", new Object[] {%s});}";
    private static final String SEND_SOURCE_FORMAT = "public void sendMessage(String method, Object[] vals) {org.kipdev.rpc.ExchangeHandler.sendMessage(this, method, vals);}";

    public static boolean writeClasses = false;

    public static void registerPackage(String pkg) {
        try {
            Reflections reflections = new Reflections(pkg, new DynamicClassLoader(), new MethodAnnotationsScanner(), new SubTypesScanner(false));

            Stream.of(RPC.class)
                    .flatMap(clazz -> reflections.getMethodsAnnotatedWith(clazz).stream())
                    .map(Method::getDeclaringClass)
                    .map(Class::getCanonicalName)
                    .distinct()
                    .forEach(name -> register(name, ClassImpactor.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerPackage(String pkg, Class<?> ref) {
        try {
            ClassPool.getDefault().appendClassPath(new ClassClassPath(ref));
            Reflections reflections = new Reflections(pkg, new DynamicClassLoader(ref.getClassLoader()), new MethodAnnotationsScanner(), new SubTypesScanner(false));

            Stream.of(RPC.class)
                    .flatMap(clazz -> reflections.getMethodsAnnotatedWith(clazz).stream())
                    .map(Method::getDeclaringClass)
                    .map(Class::getCanonicalName)
                    .distinct()
                    .forEach(name -> register(name, ref));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void register(String toLoad, Class<?> ref) {
        ClassPool.getDefault().appendClassPath(new ClassClassPath(Exchange.class));

        System.out.printf("Attempting to register %s\n", toLoad);
        try {
            CtClass ctClass = ClassPool.getDefault().getCtClass(toLoad);
            if (ctClass.isFrozen()) {
                // Hopefully it was impacted right
                System.out.println("Class already loaded.");
                return;
            }
            boolean didSomething = false;
            for (CtMethod method : getAllMethods(ctClass.getDeclaredMethods())) {
                if (method.hasAnnotation(RPC.class)) {
                    impact(ctClass, method);
                    didSomething = true;
                }
            }
            if (didSomething) {
                ctClass.toClass(ref.getClassLoader(), ref.getProtectionDomain());
                if (writeClasses) {
                    ctClass.writeFile();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void impact(CtClass ctClass, CtMethod method) throws Exception {
        String name = method.getName();
        // Renames the original method in order to use it for receive handling
        method.setName(name + "$receive");

        try {
            ctClass.getDeclaredMethod("sendMessage", ClassPool.getDefault().get(new String[]{"java.lang.String", "java.lang.Object[]"}));
        } catch (Throwable ignored) {
            CtMethod generated = CtMethod.make(SEND_SOURCE_FORMAT, ctClass);
            ctClass.addMethod(generated);
        }

        // Retrieves the method's signature and parses it into a list of compile time type names.
        String signature = method.getSignature();
        List<String> paramTypes = getTypeNamesFromSignature(signature);

        // Converts the type names into a string used for the method's parameters.
        StringJoiner joiner = new StringJoiner(",");
        for (int i1 = 0; i1 < paramTypes.size(); i1++) {
            String name1 = paramTypes.get(i1);
            String s = name1 + " v" + i1;
            joiner.add(s);
        }
        String params = joiner.toString();

        // Converts the type names into a list used in array creation, casting when necessary.
        StringJoiner paramValues = new StringJoiner(",");
        // Top of stack in non-static method is "this" instead of first parameter
        int isStatic = Modifier.isStatic(method.getModifiers()) ? 0 : 1;
        for (int i1 = 0; i1 < paramTypes.size(); i1++) {
            paramValues.add(getCastFor(paramTypes.get(i1), "$" + (i1 + isStatic)));
        }

        // Formats and compiles the new source code.
        String source = String.format(
                SOURCE_FORMAT,
                name,
                params,
                paramValues
        );
        CtMethod generated = CtMethod.make(source, ctClass);

        // This can not be included in source due to obfuscation issues
        generated.getMethodInfo().setDescriptor(signature);
        ctClass.addMethod(generated);
    }

    @SneakyThrows
    public static CtMethod[] getAllMethods(CtMethod[] methods) {
        List<CtMethod> methods1 = new ArrayList<>();
        for (CtMethod method : methods) {
            if (method.hasAnnotation(RPC.class) && method.getReturnType() == CtClass.voidType) {
                methods1.add(method);
            }
        }
        return methods1.toArray(new CtMethod[0]);
    }

    public static List<String> getTypeNamesFromSignature(String signature) {
        List<String> params = new ArrayList<>();
        signature = signature.substring(1, signature.indexOf(')'));
        for (int i = 0; i < signature.length(); i++) {
            String name;
            if (signature.charAt(i) == 'L' ) {
                int end = signature.substring(i).indexOf(';') + i;
                name = getTypeNameFromASMName(signature.substring(i + 1, end));
                i = end;
            } else if (signature.charAt(i) == '[') {
                int j = signature.substring(i).lastIndexOf('[') + 1;
                int end;
                if (signature.charAt(j) == 'L') {
                    end = signature.substring(i).indexOf(';') + i;
                } else {
                    end = j;
                }
                name = getTypeNameFromASMName(signature.substring(i, end + 1));
                i = end;
            } else {
                name = getTypeNameFromASMName(signature.substring(i, i + 1));
            }
            params.add(name);
        }
        return params;
    }

    public static String getTypeNameFromASMName(String name) {
        switch (name) {
            case "B":
                return byte.class.getName();
            case "C":
                return char.class.getName();
            case "D":
                return double.class.getName();
            case "F":
                return float.class.getName();
            case "I":
                return int.class.getName();
            case "J":
                return long.class.getName();
            case "S":
                return short.class.getName();
            case "Z":
                return boolean.class.getName();
            case "V":
                return void.class.getName();
        }
        if (name.startsWith("[")) {
            return getTypeNameFromASMName(name.replaceAll("[L;]", "").substring(1)) + "[]";
        }
        return "java.lang.Object";
    }

    public static String getCastFor(String type, String code) {
        if (type.equals("int")) return "new Integer(" + code + ")";
        if (type.equals("float")) return "new Float(" + code + ")";
        if (type.equals("byte")) return "new Byte(" + code + ")";
        if (type.equals("double")) return "new Double(" + code + ")";
        if (type.equals("long")) return "new Long(" + code + ")";
        if (type.equals("char")) return "new Character(" + code + ")";
        if (type.equals("boolean")) return "new Boolean(" + code + ")";
        if (type.equals("short")) return "new Short(" + code + ")";
        return code;
    }
}
