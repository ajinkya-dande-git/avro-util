/*
 * Copyright 2021 LinkedIn Corp.
 * Licensed under the BSD 2-Clause License (the "License").
 * See License in the project root for license information.
 */

package com.linkedin.avroutil1.parser.avsc;

import com.linkedin.avroutil1.model.AvroArraySchema;
import com.linkedin.avroutil1.model.AvroEnumSchema;
import com.linkedin.avroutil1.model.AvroFixedSchema;
import com.linkedin.avroutil1.model.AvroJavaStringRepresentation;
import com.linkedin.avroutil1.model.AvroLogicalType;
import com.linkedin.avroutil1.model.AvroMapSchema;
import com.linkedin.avroutil1.model.AvroPrimitiveSchema;
import com.linkedin.avroutil1.model.AvroRecordSchema;
import com.linkedin.avroutil1.model.AvroSchema;
import com.linkedin.avroutil1.model.AvroSchemaField;
import com.linkedin.avroutil1.model.AvroType;
import com.linkedin.avroutil1.model.AvroUnionSchema;
import com.linkedin.avroutil1.model.SchemaOrRef;
import com.linkedin.avroutil1.parser.exceptions.AvroSyntaxException;
import com.linkedin.avroutil1.parser.exceptions.JsonParseException;
import com.linkedin.avroutil1.testcommon.TestUtil;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class AvscParserTest {

    @Test
    public void testParseInvalidJson() throws Exception {
        String avsc = TestUtil.load("schemas/TestInvalidJsonRecord.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNotNull(result.getParseError());
        Assert.assertTrue(result.getParseError() instanceof JsonParseException);
    }

    @Test
    public void testParseInvalidType() throws Exception {
        String avsc = TestUtil.load("schemas/TestInvalidTypeRecord.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNotNull(result.getParseError());
        Assert.assertTrue(result.getParseError() instanceof AvroSyntaxException);
    }

    @Test
    public void testParseInvalidType2() throws Exception {
        String avsc = TestUtil.load("schemas/TestInvalidTypeRecord2.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNotNull(result.getParseError());
        Assert.assertTrue(result.getParseError() instanceof AvroSyntaxException);
    }

    @Test
    public void testParseInvalidDoc() throws Exception {
        String avsc = TestUtil.load("schemas/TestInvalidDocRecord.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNotNull(result.getParseError());
        Assert.assertTrue(result.getParseError() instanceof AvroSyntaxException);
    }

    @Test
    public void testParseInvalidField() throws Exception {
        String avsc = TestUtil.load("schemas/TestInvalidFieldRecord.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNotNull(result.getParseError());
        Assert.assertTrue(result.getParseError() instanceof AvroSyntaxException);
    }

    @Test
    public void testParseInvalidEnumSymbol() throws Exception {
        String avsc = TestUtil.load("schemas/TestInvalidEnumSymbol.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNotNull(result.getParseError());
        Assert.assertTrue(result.getParseError() instanceof AvroSyntaxException);
    }

    @Test
    public void testParseBadEnumDefault() throws Exception {
        String avsc = TestUtil.load("schemas/TestBadEnumDefault.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        AvroRecordSchema recordSchema = (AvroRecordSchema) result.getTopLevelSchema();
        List<AvscIssue> issues = result.getIssues(recordSchema);
        Assert.assertFalse(issues.stream().noneMatch(issue -> issue.getMessage().contains("default value")));
        AvroEnumSchema enumSchema = (AvroEnumSchema) recordSchema.getField("enumField").getSchema();
        issues = result.getIssues(enumSchema);
        Assert.assertFalse(issues.stream().noneMatch(issue -> issue.getMessage().contains("default value")));
    }

    @Test
    public void testParseInvalidReference() throws Exception {
        String avsc = TestUtil.load("schemas/TestInvalidReferenceRecord.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNotNull(result.getParseError());
        Assert.assertTrue(result.getParseError() instanceof AvroSyntaxException);
    }

    @Test
    public void testParseEndOfLineComment() throws Exception {
        String avsc = TestUtil.load("schemas/TestEndOfLineCommentRecord.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNotNull(result.getParseError());
        Assert.assertTrue(result.getParseError() instanceof JsonParseException);
        Assert.assertTrue(result.getParseError().getMessage().contains("comment"));
    }

    @Test
    public void testSimpleParse() throws Exception {
        String avsc = TestUtil.load("schemas/TestRecord.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNull(result.getParseError());
        AvroSchema schema = result.getTopLevelSchema();
        Assert.assertNotNull(schema);
        Assert.assertEquals(schema.type(), AvroType.RECORD);
        AvroRecordSchema recordSchema = (AvroRecordSchema) schema;
        Assert.assertEquals(recordSchema.getFullName(), "com.acme.TestRecord");
        List<AvroSchemaField> fields = recordSchema.getFields();
        Assert.assertNotNull(fields);
        Assert.assertEquals(fields.size(), 11);

        Assert.assertEquals(fields.get(0).getPosition(), 0);
        Assert.assertEquals(fields.get(0).getName(), "booleanField");
        Assert.assertEquals(fields.get(0).getSchema().type(), AvroType.BOOLEAN);

        Assert.assertEquals(fields.get(1).getPosition(), 1);
        Assert.assertEquals(fields.get(1).getName(), "intField");
        Assert.assertEquals(fields.get(1).getSchema().type(), AvroType.INT);

        Assert.assertEquals(fields.get(2).getPosition(), 2);
        Assert.assertEquals(fields.get(2).getName(), "longField");
        Assert.assertEquals(fields.get(2).getSchema().type(), AvroType.LONG);

        Assert.assertEquals(fields.get(3).getPosition(), 3);
        Assert.assertEquals(fields.get(3).getName(), "floatField");
        Assert.assertEquals(fields.get(3).getSchema().type(), AvroType.FLOAT);

        Assert.assertEquals(fields.get(4).getPosition(), 4);
        Assert.assertEquals(fields.get(4).getName(), "doubleField");
        Assert.assertEquals(fields.get(4).getSchema().type(), AvroType.DOUBLE);

        Assert.assertEquals(fields.get(5).getPosition(), 5);
        Assert.assertEquals(fields.get(5).getName(), "bytesField");
        Assert.assertEquals(fields.get(5).getSchema().type(), AvroType.BYTES);

        Assert.assertEquals(fields.get(6).getPosition(), 6);
        Assert.assertEquals(fields.get(6).getName(), "stringField");
        Assert.assertEquals(fields.get(6).getSchema().type(), AvroType.STRING);

        Assert.assertEquals(fields.get(7).getPosition(), 7);
        Assert.assertEquals(fields.get(7).getName(), "enumField");
        Assert.assertEquals(fields.get(7).getSchema().type(), AvroType.ENUM);
        AvroEnumSchema simpleEnumSchema = (AvroEnumSchema) fields.get(7).getSchema();
        Assert.assertEquals(simpleEnumSchema.getFullName(), "innerNamespace.SimpleEnum");
        Assert.assertEquals(simpleEnumSchema.getSymbols(), Arrays.asList("A", "B", "C"));

        Assert.assertEquals(fields.get(8).getPosition(), 8);
        Assert.assertEquals(fields.get(8).getName(), "fixedField");
        Assert.assertEquals(fields.get(8).getSchema().type(), AvroType.FIXED);
        Assert.assertEquals(((AvroFixedSchema)fields.get(8).getSchema()).getFullName(), "com.acme.SimpleFixed");
        Assert.assertEquals(((AvroFixedSchema)fields.get(8).getSchema()).getSize(), 7);

        Assert.assertEquals(fields.get(9).getPosition(), 9);
        Assert.assertEquals(fields.get(9).getName(), "strArrayField");
        Assert.assertEquals(fields.get(9).getSchema().type(), AvroType.ARRAY);
        Assert.assertEquals(((AvroArraySchema)fields.get(9).getSchema()).getValueSchema().type(), AvroType.NULL);

        Assert.assertEquals(fields.get(10).getPosition(), 10);
        Assert.assertEquals(fields.get(10).getName(), "enumMapField");
        Assert.assertEquals(fields.get(10).getSchema().type(), AvroType.MAP);
        AvroSchema mapValueSchema = ((AvroMapSchema) fields.get(10).getSchema()).getValueSchema();
        Assert.assertSame(mapValueSchema, simpleEnumSchema);
    }

    @Test
    public void testSelfReference() throws Exception {
        String avsc = TestUtil.load("schemas/LongList.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNull(result.getParseError());
        AvroRecordSchema schema = (AvroRecordSchema) result.getTopLevelSchema();

        //schema.next[1] == schema

        AvroSchemaField nextField = schema.getField("next");
        AvroUnionSchema union = (AvroUnionSchema) nextField.getSchema();
        SchemaOrRef secondBranch = union.getTypes().get(1);
        Assert.assertSame(secondBranch.getSchema(), schema);
    }

    @Test
    public void testMisleadingNamespace() throws Exception {
        String avsc = TestUtil.load("schemas/TestMisleadingNamespaceRecord.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNull(result.getParseError());
        AvroRecordSchema schema = (AvroRecordSchema) result.getTopLevelSchema();
        Assert.assertEquals(schema.getFullName(), "com.acme.TestMisleadingNamespaceRecord");
        AvroRecordSchema inner1 = (AvroRecordSchema) schema.getField("f1").getSchema();
        AvroRecordSchema inner2 = (AvroRecordSchema) schema.getField("f2").getSchema();
        Assert.assertEquals(inner1.getFullName(), "com.acme.SimpleName");
        Assert.assertEquals(inner2.getFullName(), "not.so.SimpleName");
        Assert.assertEquals(result.getIssues().size(), 4);
    }

    @Test
    public void testParsingDefaultValues() throws Exception {
        String avsc = TestUtil.load("schemas/TestRecordWithDefaultValues.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNull(result.getParseError());
        AvroRecordSchema schema = (AvroRecordSchema) result.getTopLevelSchema();
        for (AvroSchemaField field : schema.getFields()) {
            Assert.assertNotNull(field.getDefaultValue(), "field " + field.getName() + " has a null default");
        }
    }

    @Test
    public void testParsingHorribleDefaultValues() throws Exception {
        String avsc = TestUtil.load("schemas/TestRecordWithHorribleDefaultValues.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNull(result.getParseError());
        AvroRecordSchema schema = (AvroRecordSchema) result.getTopLevelSchema();
        Assert.assertNotNull(schema);
        for (AvroSchemaField field : schema.getFields()) {
            List<AvscIssue> issuesWithField = result.getIssues(field);
            Assert.assertFalse(issuesWithField.isEmpty(), "field " + field.getName() + " has no issues?!");
            Assert.assertFalse(issuesWithField.stream().noneMatch(issue -> issue.getMessage().contains("default value")));
            Assert.assertNull(field.getDefaultValue());
        }
    }

    @Test
    public void testParsingLogicalTypes() throws Exception {
        String avsc = TestUtil.load("schemas/TestRecordWithLogicalTypes.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNull(result.getParseError());
        AvroRecordSchema schema = (AvroRecordSchema) result.getTopLevelSchema();
        Assert.assertNotNull(schema);
        for (AvroSchemaField field : schema.getFields()) {
            switch (field.getName()) {
                case "bytesDecimalField":
                    Assert.assertEquals(field.getSchema().type(), AvroType.BYTES);
                    Assert.assertEquals(field.getSchema().logicalType(), AvroLogicalType.DECIMAL);
                    Assert.assertEquals(((AvroPrimitiveSchema)field.getSchema()).getScale(), 2);
                    Assert.assertEquals(((AvroPrimitiveSchema)field.getSchema()).getPrecision(), 4);
                    break;
                case "fixedDecimalField":
                    Assert.assertEquals(field.getSchema().type(), AvroType.FIXED);
                    Assert.assertEquals(field.getSchema().logicalType(), AvroLogicalType.DECIMAL);
                    break;
                case "fixedDurationField":
                    Assert.assertEquals(field.getSchema().type(), AvroType.FIXED);
                    Assert.assertEquals(field.getSchema().logicalType(), AvroLogicalType.DURATION);
                    break;
                case "stringUUIDField":
                    Assert.assertEquals(field.getSchema().type(), AvroType.STRING);
                    Assert.assertEquals(field.getSchema().logicalType(), AvroLogicalType.UUID);
                    break;
                case "intDateField":
                    Assert.assertEquals(field.getSchema().type(), AvroType.INT);
                    Assert.assertEquals(field.getSchema().logicalType(), AvroLogicalType.DATE);
                    break;
                case "intTimeMillisField":
                    Assert.assertEquals(field.getSchema().type(), AvroType.INT);
                    Assert.assertEquals(field.getSchema().logicalType(), AvroLogicalType.TIME_MILLIS);
                    break;
                case "longTimeMicrosField":
                    Assert.assertEquals(field.getSchema().type(), AvroType.LONG);
                    Assert.assertEquals(field.getSchema().logicalType(), AvroLogicalType.TIME_MICROS);
                    break;
                case "longTimestampMillisField":
                    Assert.assertEquals(field.getSchema().type(), AvroType.LONG);
                    Assert.assertEquals(field.getSchema().logicalType(), AvroLogicalType.TIMESTAMP_MILLIS);
                    break;
                case "longTimestampMicrosField":
                    Assert.assertEquals(field.getSchema().type(), AvroType.LONG);
                    Assert.assertEquals(field.getSchema().logicalType(), AvroLogicalType.TIMESTAMP_MICROS);
                    break;
                case "longLocalTimestampMillisField":
                    Assert.assertEquals(field.getSchema().type(), AvroType.LONG);
                    Assert.assertEquals(field.getSchema().logicalType(), AvroLogicalType.LOCAL_TIMESTAMP_MILLIS);
                    break;
                case "longLocalTimestampMicrosField":
                    Assert.assertEquals(field.getSchema().type(), AvroType.LONG);
                    Assert.assertEquals(field.getSchema().logicalType(), AvroLogicalType.LOCAL_TIMESTAMP_MICROS);
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    public void testParsingHorribleLogicalTypes() throws Exception {
        String avsc = TestUtil.load("schemas/TestRecordWithHorribleLogicalTypes.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNull(result.getParseError());
        AvroRecordSchema schema = (AvroRecordSchema) result.getTopLevelSchema();
        for (AvroSchemaField field : schema.getFields()) {
            Assert.assertNull(field.getSchema().logicalType(), "field " + field.getName()
                    + " should not have a successfully-parsed logicalType");
        }
    }

    @Test
    public void testParsingStringTypes() throws Exception {
        String avsc = TestUtil.load("schemas/TestRecordWithStringTypes.avsc");
        AvscParser parser = new AvscParser();
        AvscParseResult result = parser.parse(avsc);
        Assert.assertNull(result.getParseError());
        AvroRecordSchema schema = (AvroRecordSchema) result.getTopLevelSchema();
        Assert.assertNotNull(schema);
        for (AvroSchemaField field : schema.getFields()) {
            Assert.assertEquals(field.getSchema().type(), AvroType.STRING);
            AvroPrimitiveSchema strSchema = (AvroPrimitiveSchema) field.getSchema();
            switch (field.getName()) {
                case "vanillaStringField":
                    Assert.assertNull(strSchema.getJavaStringRepresentation());
                    break;
                case "stringFieldWithStringJavaType":
                    Assert.assertEquals(strSchema.getJavaStringRepresentation(), AvroJavaStringRepresentation.STRING);
                    break;
                case "stringFieldWithMisplacedCharSequenceJavaType":
                    Assert.assertNull(strSchema.getJavaStringRepresentation());
                    //TODO - look for a warning about the misplaced value
                    break;
                case "stringFieldWithCharSequenceJavaType":
                    Assert.assertEquals(strSchema.getJavaStringRepresentation(), AvroJavaStringRepresentation.CHAR_SEQUENCE);
                    break;
                case "stringFieldWithUtf8JavaType":
                    Assert.assertEquals(strSchema.getJavaStringRepresentation(), AvroJavaStringRepresentation.UTF8);
                    break;
            }
        }
    }

    @Test
    public void validateTestSchemas() throws Exception {
        //this test acts as a sanity check of test schemas by parsing them with the latest
        //version of avro (the reference impl)
        Schema parsed;
        Schema.Field field;

        parsed = vanillaParse("schemas/TestRecord.avsc");

        parsed = vanillaParse("schemas/TestMisleadingNamespaceRecord.avsc");
        Assert.assertEquals(parsed.getFullName(), "com.acme.TestMisleadingNamespaceRecord");
        Schema inner1 = parsed.getField("f1").schema();
        Assert.assertEquals(inner1.getFullName(), "com.acme.SimpleName");
        Schema inner2 = parsed.getField("f2").schema();
        Assert.assertEquals(inner2.getFullName(), "not.so.SimpleName");

        parsed = vanillaParse("schemas/TestRecordWithDefaultValues.avsc");

        parsed = vanillaParse("schemas/TestRecordWithLogicalTypes.avsc");

        parsed = vanillaParse("schemas/TestRecordWithStringTypes.avsc");
        field = parsed.getField("stringFieldWithStringJavaType");
        Assert.assertNull(field.getProp("avro.java.string")); //set on the type, does not become a field prop
        Assert.assertEquals(field.schema().getProp("avro.java.string"), GenericData.StringType.String.name());
        field = parsed.getField("stringFieldWithMisplacedCharSequenceJavaType");
        Assert.assertEquals(field.getProp("avro.java.string"), GenericData.StringType.CharSequence.name());
        Assert.assertNull(field.schema().getProp("avro.java.string"));

        parsed = vanillaParse("schemas/TestRecordWithHorribleLogicalTypes.avsc");
        for (Schema.Field f : parsed.getFields()) {
            Assert.assertNull(f.schema().getLogicalType(), "field " + f.name()
                    + " should not have a successfully-parsed logical type");
        }
    }

    private Schema vanillaParse(String resource) throws Exception {
        String avsc = TestUtil.load(resource);
        Schema.Parser vanillaParser = new Schema.Parser();
        vanillaParser.setValidate(true);
        vanillaParser.setValidateDefaults(true);
        Schema parsed = vanillaParser.parse(avsc);
        Assert.assertNotNull(parsed);
        return parsed;
    }
}