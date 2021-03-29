package edu.caltech.cs2.helpers;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class Inspection {
  private static String[] getUsageOf(List<String> regexps, List<? extends Node> codeObjects) {
    for (Node d : codeObjects) {
      for (String regex : regexps) {
        if (d.toString().replaceAll("\\R", "").matches(".*" + regex + ".*")) {
          return new String[]{d.toString().replaceAll("\\R", ""), regex};
        }
      }
    }
    return null;
  }

  public static void assertNoImportsOf(String filePath, List<String> regexps) {
    try {
      CompilationUnit cu = JavaParser.parse(new File(filePath));
      String[] usage = getUsageOf(regexps, cu.getImports());
      if (usage != null) {
        fail(usage[0] + " is a banned import under " + usage[1] + " in " + Paths.get(filePath).getFileName()
                + ".");
      }
    } catch (FileNotFoundException e) {
      fail("Missing Java file: " + Paths.get(filePath).getFileName());
    }
  }

  public static void assertNoImportsOfExcept(String filePath, String bannedImport, List<String> allowedClasses) {
    try {
      CompilationUnit cu = JavaParser.parse(new File(filePath));
      String bannedRegex = bannedImport + "\\.(?!" + String.join("|", allowedClasses) + ")";
      String[] usage = getUsageOf(List.of(bannedRegex), cu.getImports());
      if (usage != null) {
        fail(usage[0] + " is a banned import under " + bannedImport +
                " and is not an allowed import " +
                allowedClasses + " in " + Paths.get(filePath).getFileName() + ".");
      }
    } catch (FileNotFoundException e) {
      fail("Missing Java file: " + Paths.get(filePath).getFileName());
    }
  }

  private static class ConstructorCollector extends VoidVisitorAdapter<List<ConstructorDeclaration>> {
    @Override
    public void visit(ConstructorDeclaration md, List<ConstructorDeclaration> collector) {
      super.visit(md, collector);
      collector.add(md);
    }
  }

  private static class MethodCollector extends VoidVisitorAdapter<List<MethodDeclaration>> {
    @Override
    public void visit(MethodDeclaration md, List<MethodDeclaration> collector) {
      super.visit(md, collector);
      collector.add(md);
    }
  }

  private static MethodCollector METHOD_COLLECTOR = new MethodCollector();
  private static ConstructorCollector CONSTRUCTOR_COLLECTOR = new ConstructorCollector();

  public static void assertNoUsageOf(String filePath, List<String> regexps) {
    try {
      CompilationUnit cu = JavaParser.parse(new File(filePath));

      List<ConstructorDeclaration> constructors = new ArrayList<>();
      CONSTRUCTOR_COLLECTOR.visit(cu, constructors);
      String[] usage = getUsageOf(regexps, constructors);
      if (usage != null) {
        fail("You may not use " + usage[1] + " in " + Paths.get(filePath).getFileName() + ".");
      }

      List<MethodDeclaration> methods = new ArrayList<>();
      METHOD_COLLECTOR.visit(cu, methods);
      usage = getUsageOf(regexps, methods);
      if (usage != null) {
        fail("You may not use " + usage[1] + " in " + Paths.get(filePath).getFileName() + ".");
      }
    } catch (FileNotFoundException e) {
      fail("Missing Java file: " + Paths.get(filePath).getFileName());
    }
  }

  public static void assertConstructorHygiene(String filePath) {
    try {
      CompilationUnit cu = JavaParser.parse(new File(filePath));
      List<ConstructorDeclaration> constructors = new ArrayList<>();
      CONSTRUCTOR_COLLECTOR.visit(cu, constructors);
      int nonThisConstructors = 0;
      for (ConstructorDeclaration c : constructors) {
        BlockStmt body = c.getBody();
        List<Statement> statements = body.getStatements();
        if (statements.size() != 1) {
          nonThisConstructors++;
        } else if (!statements.get(0).toString().startsWith("this(")) {
          nonThisConstructors++;
        }

        if (nonThisConstructors > 1) {
          fail("All but one of your constructors must use the this(...) notation.");
        }
      }

    } catch (FileNotFoundException e) {
      fail("Missing Java file: " + Paths.get(filePath).getFileName());
    }
  }
}
