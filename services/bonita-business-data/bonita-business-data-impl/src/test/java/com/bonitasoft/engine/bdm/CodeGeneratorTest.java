package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

/**
 * @author Romain Bioteau
 */
public class CodeGeneratorTest {

    private CodeGenerator codeGenerator;

    @Before
    public void setUp() throws Exception {
        codeGenerator = new CodeGenerator();
    }

    @Test
    public void shouldAddClass_AddAJDefinedClassInModel_AndReturnIt() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        assertThat(definedClass).isNotNull().isInstanceOf(JDefinedClass.class);
        assertThat(definedClass.name()).isEqualTo("Entity");
        assertThat(definedClass.fullName()).isEqualTo("org.bonitasoft.Entity");
        assertThat(codeGenerator.getModel()._getClass("org.bonitasoft.Entity")).isNotNull().isSameAs(definedClass);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddClass_ThrowAnIllegalArgumentExcpetionForEmptyName() throws Exception {
        codeGenerator.addClass("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddClass_ThrowAnIllegalArgumentExcpetionForNullName() throws Exception {
        codeGenerator.addClass(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddClass_ThrowAnIllegalArgumentExcpetionForInvalidName() throws Exception {
        codeGenerator.addClass("org.bonitasoft*.Entity");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddField_ThrowAnIllegalArgumentExcpetionForEmptyName() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        codeGenerator.addField(definedClass, "", String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddField_ThrowAnIllegalArgumentExcpetionForNullName() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        codeGenerator.addField(definedClass, null, String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddField_ThrowAnIllegalArgumentExcpetionForInvalidName() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        codeGenerator.addField(definedClass, "enum", String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddField_ThrowAnIllegalArgumentExcpetionForNullClassType() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        codeGenerator.addField(definedClass, "name", (Class<?>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldAddField_ThrowAnIllegalArgumentExcpetionForNullJType() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        codeGenerator.addField(definedClass, "name", (JClass) null);
    }

    @Test
    public void shouldAddPrivateField_FromClass_AddAJVarFieldInDefinedClass_AndReturnIt() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        final JFieldVar privateField = codeGenerator.addField(definedClass, "name", String.class);
        assertThat(privateField).isNotNull().isInstanceOf(JFieldVar.class);
        assertThat(privateField.name()).isEqualTo("name");
        assertThat(privateField.type().name()).isEqualTo(String.class.getSimpleName());
        assertThat(codeGenerator.getModel()._getClass("org.bonitasoft.Entity").fields()).containsEntry("name", privateField);
    }

    @Test
    public void shouldAddPrivateField_FromJType_AddAJVarFieldInDefinedClass_AndReturnIt() throws Exception {
        final JDefinedClass employeeClass = codeGenerator.addClass("org.bonitasoft.Employee");
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        final JFieldVar privateField = codeGenerator.addField(definedClass, "employee", employeeClass);
        assertThat(privateField).isNotNull().isInstanceOf(JFieldVar.class);
        assertThat(privateField.name()).isEqualTo("employee");
        assertThat(privateField.type().name()).isEqualTo("Employee");
        assertThat(codeGenerator.getModel()._getClass("org.bonitasoft.Entity").fields()).containsEntry("employee", privateField);

        final JClass stringList = codeGenerator.getModel().ref(List.class).narrow(String.class);
        final JFieldVar collectionField = codeGenerator.addField(definedClass, "skills", stringList);
        assertThat(collectionField).isNotNull();
        assertThat(collectionField.type().name()).isEqualTo(List.class.getSimpleName() + "<" + String.class.getSimpleName() + ">");
    }

    @Test
    public void shouldAddAnnotation_AddAJAnnotation_AndReturnIt() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        final JAnnotationUse annotation = codeGenerator.addAnnotation(definedClass, Deprecated.class);
        assertThat(annotation).isNotNull().isInstanceOf(JAnnotationUse.class);
        assertThat(annotation.getAnnotationClass().fullName()).isEqualTo(Deprecated.class.getName());
    }

    @Test
    public void shouldAddSetter_AddAJMethodInDefinedClass_AndReturnIt() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        final JFieldVar privateField = codeGenerator.addField(definedClass, "name", String.class);
        final JMethod setter = codeGenerator.addSetter(definedClass, privateField);
        assertThat(setter).isNotNull().isInstanceOf(JMethod.class);
        assertThat(setter.name()).isEqualTo("setName");
        assertThat(codeGenerator.getModel()._getClass("org.bonitasoft.Entity").methods()).contains(setter);
    }

    @Test
    public void shouldAddGetter_AddAJMethodInDefinedClass_AndReturnIt() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        final JFieldVar privateField = codeGenerator.addField(definedClass, "name", String.class);
        final JMethod setter = codeGenerator.addGetter(definedClass, privateField);
        assertThat(setter).isNotNull().isInstanceOf(JMethod.class);
        assertThat(setter.name()).isEqualTo("getName");
        assertThat(codeGenerator.getModel()._getClass("org.bonitasoft.Entity").methods()).contains(setter);
    }

    @Test
    public void shouldAddGetter_AddAJMethodInDefinedClass_AndReturnIt_ForBoolean() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        final JFieldVar privateField = codeGenerator.addField(definedClass, "married", Boolean.class);
        final JMethod setter = codeGenerator.addGetter(definedClass, privateField);
        assertThat(setter).isNotNull().isInstanceOf(JMethod.class);
        assertThat(setter.name()).isEqualTo("isMarried");
        assertThat(codeGenerator.getModel()._getClass("org.bonitasoft.Entity").methods()).contains(setter);
    }

    @Test
    public void shouldCheckAnnotationTarget_IsValid() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        codeGenerator.addAnnotation(definedClass, Deprecated.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckAnnotationTarget_ThrowIllegalArgumentExceptionForType() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        codeGenerator.addAnnotation(definedClass, Basic.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckAnnotationTarget_ThrowIllegalArgumentExceptionForField() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        final JFieldVar privateField = codeGenerator.addField(definedClass, "name", String.class);
        codeGenerator.addAnnotation(privateField, Entity.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldCheckAnnotationTarget_ThrowIllegalArgumentExceptionForMethod() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        final JFieldVar privateField = codeGenerator.addField(definedClass, "name", String.class);
        codeGenerator.addAnnotation(privateField, Entity.class);
    }

    @Test
    public void shoulAddInterface_AddInterface_ToADefinedClass() throws Exception {
        JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        definedClass = codeGenerator.addInterface(definedClass, Serializable.class.getName());
        assertThat(definedClass._implements()).isNotEmpty().contains(codeGenerator.getModel().ref(Serializable.class.getName()));
    }

    @Test
    public void shouldGenerate_CreatePojoFile() throws Exception {
        final JDefinedClass definedClass = codeGenerator.addClass("org.bonitasoft.Entity");
        final JFieldVar nameField = codeGenerator.addField(definedClass, "name", String.class);

        final JClass stringList = codeGenerator.getModel().ref(List.class).narrow(String.class);
        final JFieldVar skillField = codeGenerator.addField(definedClass, "skills", stringList);
        final JFieldVar dateField = codeGenerator.addField(definedClass, "returnDate", codeGenerator.getModel().ref(Date.class));
        final JAnnotationUse tAnnotation = codeGenerator.addAnnotation(dateField, Temporal.class);
        tAnnotation.param("value", TemporalType.TIMESTAMP);

        codeGenerator.addGetter(definedClass, nameField);
        codeGenerator.addSetter(definedClass, nameField);
        codeGenerator.addAnnotation(definedClass, Entity.class);

        codeGenerator.addGetter(definedClass, skillField);
        codeGenerator.addSetter(definedClass, skillField);

        final File destDir = createTempDirectory("generatedPojo");
        try {
            codeGenerator.generate(destDir);
            final File rootFolder = new File(destDir, "org" + File.separatorChar + "bonitasoft");
            assertThat(rootFolder.listFiles()).isNotEmpty().contains(new File(rootFolder, "Entity.java"));
        } finally {
            FileUtils.deleteQuietly(destDir);
        }
    }

    @Test
    public void shouldToJavaClass_ReturnIntegerClass() throws Exception {
        assertThat(codeGenerator.toJavaClass(FieldType.INTEGER).name()).isEqualTo(Integer.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnStringClass() throws Exception {
        assertThat(codeGenerator.toJavaClass(FieldType.STRING).name()).isEqualTo(String.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnLongClass() throws Exception {
        assertThat(codeGenerator.toJavaClass(FieldType.LONG).name()).isEqualTo(Long.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnDoubleClass() throws Exception {
        assertThat(codeGenerator.toJavaClass(FieldType.DOUBLE).name()).isEqualTo(Double.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnFloatClass() throws Exception {
        assertThat(codeGenerator.toJavaClass(FieldType.FLOAT).name()).isEqualTo(Float.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnBooleanClass() throws Exception {
        assertThat(codeGenerator.toJavaClass(FieldType.BOOLEAN).name()).isEqualTo(Boolean.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnDateClass() throws Exception {
        assertThat(codeGenerator.toJavaClass(FieldType.DATE).name()).isEqualTo(Date.class.getSimpleName());
    }

    @Test
    public void shouldToJavaClass_ReturnStringTextClass() throws Exception {
        assertThat(codeGenerator.toJavaClass(FieldType.TEXT).name()).isEqualTo(String.class.getSimpleName());
    }

    protected File createTempDirectory(final String tmpDirName) throws IOException {
        final File destDir = File.createTempFile(tmpDirName, null);
        destDir.delete();
        destDir.mkdirs();
        return destDir;
    }

    @Test
    public void shouldAddEqualsMethod_GenerateAnEqualsMethod_BasedOnDefinedClassFields() throws Exception {
        System.err
        .println("***************** PLEASE Implement test com.bonitasoft.engine.bdm.CodeGeneratorTest.shouldAddEqualsMethod_GenerateAnEqualsMethod_BasedOnDefinedClassFields() *************");
    }

}
