/*
 * @author Linghui Luo
 */
package magpiebridge.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.classLoader.ClassLoaderFactory;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.SourceDirectoryTreeModule;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.warnings.Warnings;

import soot.G;
import soot.Scene;
import soot.options.Options;

public class WalaToSootIRConverter {
  private IClassHierarchy classHierarchy;
  public AnalysisScope scope;
  public ClassLoaderFactory factory;
  private ClassConverter classConverter;
  private final File walaPropertiesFile = new File("target/classes/wala.properties");

  public WalaToSootIRConverter(@Nonnull Set<String> sourcePath) {
    this(sourcePath, Collections.emptySet(), null);
  }

  public WalaToSootIRConverter(@Nonnull Set<String> sourcePath, @Nonnull Set<String> libPath) {
    this(sourcePath, libPath, null);
  }

  public WalaToSootIRConverter(
      @Nonnull Set<String> sourcePath, @Nonnull Set<String> libPath, String exclusionFilePath) {
    initializeSoot(libPath);
    this.classConverter = new ClassConverter();
    addScopesForJava();
    // add the source directory to scope
    for (String path : sourcePath) {
      scope.addToScope(
          JavaSourceAnalysisScope.SOURCE, new SourceDirectoryTreeModule(new File(path)));
    }
    try {
      // add Jars to scope
      for (String libJar : libPath) {
        scope.addToScope(ClassLoaderReference.Primordial, new JarFile(libJar));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    setExclusions(exclusionFilePath);
    factory = new ECJClassLoaderFactory(scope.getExclusions());
  }

  /**
   * Initialize soot options, override this if you want to set up the options differently.
   *
   * @param libPath the library path
   */
  protected void initializeSoot(Set<String> libPath) {
    G.v();
    G.reset();
    StringBuilder longLibPath = new StringBuilder();
    libPath.forEach(
        s -> {
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
  }

  private void setExclusions(@Nullable String exclusionFilePath) {
    if (exclusionFilePath == null) {
      return;
    }

    File exclusionFile = new File(exclusionFilePath);
    if (exclusionFile.isFile()) {
      FileOfClasses classes;
      try {
        classes = new FileOfClasses(new FileInputStream(exclusionFile));
        scope.setExclusions(classes);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      throw new RuntimeException("the given path to the exclusion file does not point to a file.");
    }
  }

  private void addScopesForJava() {
    createWalaproperties();
    // disable System.err messages generated from eclipse jdt
    System.setProperty("wala.jdt.quiet", "true");
    scope = new JavaSourceAnalysisScope();
    try {
      // add standard libraries to scope
      String[] stdlibs = WalaProperties.getJ2SEJarFiles();
      for (String stdlib : stdlibs) {

        scope.addToScope(ClassLoaderReference.Primordial, new JarFile(stdlib));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Create wala.properties to class path */
  private void createWalaproperties() {
    if (!walaPropertiesFile.exists()) {
      PrintWriter pw;
      try {
        pw = new PrintWriter(walaPropertiesFile);
        String jdkPath = System.getProperty("java.home");
        pw.println("java_runtime_dir = " + new File(jdkPath).toString().replace("\\", "/"));
        pw.close();
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /** Use WALA's JAVA source code front-end to build class hierarchy. */
  private void buildClassHierachy() {
    try {
      this.classHierarchy = ClassHierarchyFactory.make(scope, factory);
      Warnings.clear();
    } catch (ClassHierarchyException e) {
      throw new RuntimeException(e);
    }
  }

  private Iterator<IClass> iterateWalaJavaSourceClasses() {
    if (classHierarchy == null) {
      try {
        buildClassHierachy();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    classHierarchy.getLoaders();
    return classHierarchy.getLoader(JavaSourceAnalysisScope.SOURCE).iterateAllClasses();
  }

  public void convert() {
    Iterator<IClass> it = iterateWalaJavaSourceClasses();
    while (it.hasNext()) {
      IClass klass = it.next();
      JavaClass walaClass = (JavaClass) klass;
      this.classConverter.convertClass(walaClass);
    }
  }
}
