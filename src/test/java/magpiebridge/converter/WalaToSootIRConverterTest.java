package magpiebridge.converter;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import soot.G;
import soot.Scene;
import soot.options.Options;

public class WalaToSootIRConverterTest {

	@Test
	public void test() {
		Set<String> sourcePath = new HashSet<>();
		sourcePath.add(new File("src/test/resources/minimaltestsuite/java6").getAbsolutePath());
		Set<String> libPath = new HashSet<>();
		String exclusionFilePath = null;

		G.v();
		G.reset();
		StringBuilder longLibPath = new StringBuilder();
		libPath.forEach(s -> {
			longLibPath.append(s);
			longLibPath.append(File.pathSeparator);
		});
		Options.v().set_soot_classpath(longLibPath.toString());
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_prepend_classpath(true);
		Options.v().set_full_resolver(true);

		Scene.v().loadNecessaryClasses();

		WalaToSootIRConverter converter=new WalaToSootIRConverter(sourcePath, libPath, exclusionFilePath);
		converter.convert();

	}
}
