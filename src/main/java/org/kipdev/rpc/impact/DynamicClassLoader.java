package org.kipdev.rpc.impact;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by Justin
 *
 * Used to load all classes that will be modified. Does not actually load anything on main class loader
 */
public class DynamicClassLoader extends URLClassLoader {

    public DynamicClassLoader(URL...url) {
        this(DynamicClassLoader.class.getClassLoader(), url);
    }

    public DynamicClassLoader(ClassLoader loader, URL...url) {
        super(url, loader);
        if (loader instanceof URLClassLoader) {
            for (URL url1 : ((URLClassLoader) loader).getURLs()) {
                this.addURL(url1);
            }
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClassDirect(name, resolve);
    }

    protected Class<?> loadClassDirect(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    c = findClass(name);
                } catch (ClassNotFoundException e) {
                    // ClassNotFoundException thrown if class not found
                    // from the non-null parent class loader
                }

                if (c == null) {
                    // If still not found, then invoke findClass in order
                    // to find the class.
                    if (getParent() != null)
                        c = getParent().loadClass(name);
                    else
                        throw new ClassNotFoundException(name);
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
}
