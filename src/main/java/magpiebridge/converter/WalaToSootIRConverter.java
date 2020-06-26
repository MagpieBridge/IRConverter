/*
 * @author Linghui Luo
 */
package magpiebridge.converter;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.classLoader.ClassLoaderFactory;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.classLoader.SourceDirectoryTreeModule;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.warnings.Warnings;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarFile;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import soot.G;
import soot.Scene;
import soot.options.Options;

/**
 * The Class WalaToSootIRConverter.
 *
 * @author Linghui Luo
 * @author João Pereira
 */
public class WalaToSootIRConverter {

  /** The class hierarchy. */
  protected IClassHierarchy classHierarchy;

  /** The wala analysis scope. */
  protected AnalysisScope scope;

  /** The wala class loader factory. */
  protected ClassLoaderFactory factory;

  /** The class converter. */
  private ClassConverter classConverter;

  /** The wala options. */
  protected WalaOptions walaOptions;

  /**
   * Instantiates a new wala to soot IR converter.
   *
   * @param sourcePath the set of paths to source code directories
   */
  public WalaToSootIRConverter(@Nonnull Set<String> sourcePath) {
    this(sourcePath, Collections.emptySet(), null);
  }

  /**
   * Instantiates a new wala to soot IR converter.
   *
   * @param sourcePath the set of paths to source code directories
   * @param libPath the set of paths to library code, either .jar file or .class file. Java standard
   *     libraries are added by default.
   */
  public WalaToSootIRConverter(@Nonnull Set<String> sourcePath, @Nonnull Set<String> libPath) {
    this(sourcePath, libPath, null);
  }

  /**
   * Instantiates a new wala to soot IR converter.
   *
   * @param sourcePath the set of paths to source code directories
   * @param libPath the set of paths to library code, either .jar file or .class file. Java standard
   *     libraries are added by default.
   * @param exclusionFilePath the exclusion file path contains classes you want to exclude
   * @param walaOptions the wala options
   */
  public WalaToSootIRConverter(
      @Nonnull Set<String> sourcePath,
      @Nonnull Set<String> libPath,
      String exclusionFilePath,
      WalaOptions walaOptions) {
    this.walaOptions = walaOptions;
    initializeSoot(libPath);
    this.classConverter = new ClassConverter();
    addScopesForJava();
    // add the source directory to scope
    for (String path : sourcePath) {
      scope.addToScope(
          JavaSourceAnalysisScope.SOURCE, new SourceDirectoryTreeModule(new File(path)));
    }
    try {
      scope.addToScope(buildLibPathScope(libPath, ClassLoaderReference.Extension));
    } catch (IOException | InvalidClassFileException e) {
      e.printStackTrace();
    }
    setExclusions(exclusionFilePath);
    factory = new ECJClassLoaderFactory(scope.getExclusions());
  }

  /**
   * Instantiates a new wala to soot IR converter.
   *
   * @param sourcePath the set of paths to source code directories
   * @param libPath the set of paths to library code, either .jar file or .class file. Java standard
   *     libraries are added by default.
   * @param exclusionFilePath the exclusion file path contains classes you want to exclude
   */
  public WalaToSootIRConverter(
      @Nonnull Set<String> sourcePath, @Nonnull Set<String> libPath, String exclusionFilePath) {
    this(sourcePath, libPath, exclusionFilePath, new WalaOptions());
  }

  /**
   * Instantiates a new wala to soot IR converter.
   *
   * @param files the source code files.
   * @param libPath the set of paths to library code, either .jar file or .class file. Java standard
   *     libraries are added by default.
   */
  public WalaToSootIRConverter(@Nonnull Collection<? extends Module> files, Set<String> libPath) {
    this(files, libPath, new WalaOptions());
  }

  /**
   * Instantiates a new wala to soot IR converter.
   *
   * @param files the source code files.
   * @param libPath the set of paths to library code, either .jar file or .class file. Java standard
   *     libraries are added by default.
   * @param walaOptions the wala options
   */
  public WalaToSootIRConverter(
      @Nonnull Collection<? extends Module> files, Set<String> libPath, WalaOptions walaOptions) {
    this.walaOptions = walaOptions;
    initializeSoot(libPath);
    this.classConverter = new ClassConverter();
    addScopesForJava();
    for (Module file : files) {
      scope.addToScope(JavaSourceAnalysisScope.SOURCE, file);
    }
    try {
      scope.addToScope(buildLibPathScope(libPath, ClassLoaderReference.Extension));
    } catch (IOException | InvalidClassFileException e) {
      e.printStackTrace();
    }
    setExclusions(null);
    factory = new ECJClassLoaderFactory(scope.getExclusions());
  }

  /**
   * Builds the analysis scope for library code. It looks for .jar and .class files in the given
   * libPath and add them to the scope.
   *
   * @param libPath the set of paths to library code.
   * @param loader the loader
   * @return the analysis scope
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidClassFileException the invalid class file exception
   */
  private AnalysisScope buildLibPathScope(
      final Collection<String> libPath, final ClassLoaderReference loader)
      throws IOException, InvalidClassFileException {
    final AnalysisScope analysisScope = AnalysisScope.createJavaAnalysisScope();
    for (final String pathString : libPath) {
      final Path path = Paths.get(pathString);
      Iterator<Path> pathIt = Files.walk(path).iterator();
      while (pathIt.hasNext()) {
        addToScope(analysisScope, loader, pathIt.next());
      }
    }
    return analysisScope;
  }

  /**
   * Adds the file to the given scope.
   *
   * @param analysisScope the analysis scope
   * @param loader the loader
   * @param filePath the file path
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InvalidClassFileException the invalid class file exception
   */
  private void addToScope(
      final AnalysisScope analysisScope, final ClassLoaderReference loader, final Path filePath)
      throws IOException, InvalidClassFileException {
    final File file = filePath.toFile();
    if (!file.isDirectory()) {
      final String fileName = file.getName();
      if (fileName.endsWith(".class")) {
        analysisScope.addClassFileToScope(loader, file);
      } else if (fileName.endsWith(".jar")) {
        analysisScope.addToScope(loader, new JarFile(file));
      }
      // Notice that it ignores all other files
    }
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

  /** Adds the scopes for java stand library. */
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
    File walaPropertiesFile = new File(walaOptions.getPathOfWalaProperties());
    if (!walaPropertiesFile.exists()) {
      try {
        PrintWriter pw;
        walaPropertiesFile.createNewFile();
        System.err.println(walaPropertiesFile.getAbsolutePath());
        pw = new PrintWriter(walaPropertiesFile);
        String jdkPath = System.getProperty("java.home");
        pw.println("java_runtime_dir = " + new File(jdkPath).toString().replace("\\", "/"));
        pw.close();
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }

  /** Use WALA's JAVA source code front-end to build class hierarchy. */
  protected void buildClassHierachy() {
    try {
      if (!walaOptions.allowPhantomClass()) {
        this.classHierarchy = ClassHierarchyFactory.make(scope, factory);
      } else {
        this.classHierarchy = ClassHierarchyFactory.makeWithRoot(scope, factory);
      }
      Warnings.clear();
    } catch (ClassHierarchyException e) {
      throw new RuntimeException(e);
    }
  }

  protected Iterator<IClass> iterateWalaJavaSourceClasses() {
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
