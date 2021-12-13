import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JndiLookup {
	public static void main(String[] args) {
		boolean enableJndi = false;
		String string_cmdline = null;
		int p=0;

		if (args.length > 0) {
			if (args[0].equals("-X")) {
				enableJndi = true;
				p=1;
			}
		}

		if (args.length > p) {
			string_cmdline = args[p];
		}

		try {
			String jarpath = new File(JndiLookup.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath();
			File jar1,jar2;
			if (enableJndi) {
				jar1 = new File(jarpath+"/log4j/log4j-core-2.14.1.jar");
				jar2 = new File(jarpath+"/log4j/log4j-api-2.14.1.jar");
			}
			else {
				jar1 = new File(jarpath+"/log4j/log4j-core-2.15.0.jar");
				jar2 = new File(jarpath+"/log4j/log4j-api-2.15.0.jar");
			}
			ClassLoader loader = URLClassLoader.newInstance(new URL[] { jar1.toURL(), jar2.toURL() }, JndiLookup.class.getClassLoader());

			Class c_strlookup = Class.forName("org.apache.logging.log4j.core.lookup.StrLookup", true, loader);

			Object jndi_lookup_instance = Proxy.newProxyInstance(loader, new Class<?>[]{c_strlookup}, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					String method_name = method.getName();
					Class<?>[] classes = method.getParameterTypes();

					if (method_name.equals("lookup")) {
						switch (classes.length) {
						 case 1:
							 return "jndi:"+args[0];
						 case 2:
							 return "jndi:"+args[1];
						 default:
							 return null;
						}
					}
					return null;
				}
			});

			Class c_interpolator = Class.forName("org.apache.logging.log4j.core.lookup.Interpolator", true, loader);
			Constructor ct_interpolator = c_interpolator.getConstructor();
			Object interpolator_c = ct_interpolator.newInstance();

			if (!enableJndi) {
				// Add jndi lookup if JNDI is not enabled so that we keep a "jndi:" substring
				Field strLookupMapField = c_interpolator.getDeclaredField("strLookupMap");
				strLookupMapField.setAccessible(true);
				Map<String, Object> strLookupMap = (Map<String, Object>)strLookupMapField.get(interpolator_c);
				strLookupMap.put("jndi", jndi_lookup_instance);
			}

			Class c_strsubst = Class.forName("org.apache.logging.log4j.core.lookup.StrSubstitutor", true, loader);
			Constructor ct_strsubst = c_strsubst.getConstructor(c_strlookup);
			Object strsubst_c = ct_strsubst.newInstance(interpolator_c);

			Method m_replace = c_strsubst.getMethod("replace", String.class);

			if (string_cmdline != null) {
				// Use string from cmd line
				String newstr;
				try {
					newstr = (String)m_replace.invoke(strsubst_c, string_cmdline);
				} catch (Exception e) {
					System.err.println("Exceptgion : "+e);
					// If you have an exception, keep the original string
					newstr = string_cmdline;
				}
				System.out.println(newstr);
				return;
			}

			// Read lines from stdin and replace them with lookup
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String input;
			do {
				input = reader.readLine();
				if ((input!=null) && (input.length() > 0)) {
					String newstr;
					try {
						newstr = (String)m_replace.invoke(strsubst_c, input);
					} catch (Exception e) {
						// If you have an exception, keep the original string
						newstr = input;
					}
					System.out.println(newstr);
				}
			} while ((input!=null) && (input.length() > 0));
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
