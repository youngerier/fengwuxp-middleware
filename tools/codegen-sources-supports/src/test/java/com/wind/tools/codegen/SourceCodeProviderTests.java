package com.wind.tools.codegen;


import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * @author wuxp
 * @date 2025-08-29 16:26
 **/
@Slf4j
class SourceCodeProviderTests {


    private final SourceCodeProvider sourceCodeProvider = new SourceCodeProvider();

    /**
     * test enum
     */
    enum TestEnum {
        TEST_V_1,
        TEST_V_2;
    }

    @Test
    void getTypeDeclaration() {
        Optional<ClassOrInterfaceDeclaration> typeDeclaration = sourceCodeProvider.getTypeDeclaration(SourceCodeProviderTests.class);
        Assertions.assertTrue(typeDeclaration.isPresent());
    }

    @Test
    void getClassDeclaration() {
        Optional<ClassOrInterfaceDeclaration> classDeclaration = sourceCodeProvider.getInterfaceDeclaration(SourceCodeProviderTests.class);
        Assertions.assertFalse(classDeclaration.isPresent());
    }

    @Test
    void getEnumDeclaration() {
        Optional<EnumDeclaration> annotationDeclaration = sourceCodeProvider.getEnumDeclaration(TestEnum.class);
        Assertions.assertTrue(annotationDeclaration.isPresent());
    }

    @Test
    void getAnnotationDeclaration() {
        Optional<AnnotationDeclaration> annotationDeclaration = sourceCodeProvider.getAnnotationDeclaration(Test.class);
        Assertions.assertTrue(annotationDeclaration.isPresent());
    }

    @Test
    void getCompilationUnit() {
        Optional<CompilationUnit> compilationUnit = sourceCodeProvider.getCompilationUnit(SourceCodeProviderTests.class);
        Assertions.assertTrue(compilationUnit.isPresent());
    }

    @Test
    void getFieldDeclaration() throws Exception {
        Optional<FieldDeclaration> fieldDeclaration = sourceCodeProvider.getFieldDeclaration(ExampleSourceObject.class.getField("name"));
        Assertions.assertTrue(fieldDeclaration.isPresent());
    }

    @Test
    void getMethodDeclaration() throws Exception {
        Optional<MethodDeclaration> methodDeclaration = sourceCodeProvider.getMethodDeclaration(ExampleSourceObject.class.getMethod("setName", String.class));
        Assertions.assertTrue(methodDeclaration.isPresent());
        MethodDeclaration declaration = methodDeclaration.get();
        Optional<Comment> commentOptional = declaration.getComment();
        Assertions.assertTrue(commentOptional.isPresent());
        Comment comment = commentOptional.get();
        Javadoc javadoc = comment.asJavadocComment().parse();
        List<JavadocBlockTag> blockTags = javadoc.getBlockTags();
        Assertions.assertNotNull(blockTags);
        List<JavadocDescriptionElement> elements = javadoc.getDescription().getElements();
        Assertions.assertNotNull(elements);
    }

    @Test
    void getMethodParameter() throws Exception {
        Method getName = ExampleSourceObject.class.getMethod("setName", String.class);
        Optional<Parameter> methodParameter = sourceCodeProvider.getMethodParameter(getName.getParameters()[0]);
        Assertions.assertTrue(methodParameter.isPresent());
    }

    @Test
    void getEnumConstantDeclaration() throws Exception {
        Optional<EnumConstantDeclaration> constantDeclaration = sourceCodeProvider.getEnumConstantDeclaration(TestEnum.class.getField("TEST_V_1"));
        Assertions.assertTrue(constantDeclaration.isPresent());
    }

    /**
     * use test case
     */
    @Data
    public static class ExampleSourceObject {

        /**
         * example name
         * -- GETTER --
         * 获取名称
         *
         * @return 用户名称
         */
        public String name;

        /**
         * set name
         *
         * @param name 名称
         */
        public void setName(String name) {
        }
    }

}
