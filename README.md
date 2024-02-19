# JSON Schema Generator

### Overview
An opinionated JSON Schema generator implemented in Scala.<br>
It’s designed to simplify the process of defining JSON Schemas for a modular application’s backend. 
The goal is to provide developers with a user-friendly interface, based on Scala case classes and supplemented with builder utility functions, 
that allows them to define the structure of their data easily. <br>

__The project use **Pekko Http**, as such upon running, generated json schemas are rendered as a `JsObject` from url: http://localhost/schema__

<p>The generator is part of a larger application where the backend, built with Scala, defines a generic schema that the frontend, running on Angular 2 with Angular Formly, 
can dynamically interpret to generate forms. This schema definition is crucial for developers implementing the application as it provides the structure for the data interchange
between the frontend and backend. </p>

### Why JSON Schema Generator?

*JSON Schema* is a powerful tool for validating the structure of JSON data. However, writing JSON Schemas by hand can be tedious and error-prone. <br>
This project aims to simplify the process by providing a set of builder functions that allow developers to construct schemas programmatically.
By adopting this approach, we aim to make it easier for developers to use the application’s backend and integrate it with the frontend project, 
ultimately facilitating faster development and reducing potential errors.

*Benefits*
1. **Reduced Complexity:** Developers don’t need to learn the details of JSON Schema, simplifying the development process. (well, something little doesn't hurt! :) )
2. **Type Safety:** Scala’s type system ensures type safety, which can help catch errors at compile time.
3. **Consistency:** By providing predefined models or case classes, we can ensure consistency in the schema definitions across different projects.
4. **Ease of Maintenance:** Updating or modifying the schema becomes easier as changes can be made at the abstraction level, and those changes will reflect across all projects using the schema.

### Features
| Feature | Description                                                                              |
|---------|------------------------------------------------------------------------------------------|
| Basic data types | Support for string, number, object, array, boolean, and null data types.                 |
| String formats | Predefined formats such as date, time, date-time, duration, regex, email, uri, and uuid. |
| Conditional subschemas | Support for `if-then-else` and `not` validation keywords.                                |
| Combining schemas | Support for `anyOf`, `oneOf`, and `allOf` validation keywords.                           |
| Reusable schemas | Support for the `$ref` and `$def` keywords to reference reusable schemas.                |
| Constant and enumeration | Support for `const` and `enum` validation keywords.                                          |

### How to use (simple use)
```
  val props: Map[String, SchemaElement] =
    Map(
      "firstName" -> stringField("First Name").withMinLength(3),
      "lastName" -> stringField("Last Name"),
      "age" -> numberField("Age").withMin(18),
      "email" -> stringField("Email address").withFormat(FieldFormat.email),
      "address" -> objectField("Address")
        .withObjProperties(
          Map(
            "city" -> stringField("City"),
            "postalCode" -> numberField("Postal code"),
            "country" -> stringField("Country")
          )
        )
        .withObjRequired("address" :: Nil)
        .withAdditionalProperties(Left(false))
    )

  private val userSchema: JsonSchemaForm = jsonSchemaForm("user", "object")
    .withTitle("User")
    .withDescription("A user in the system")
    .withProperties(props)
    .withRequired("firstName" :: "lastName" :: "age" :: Nil)
```

### Extra
This projects also implements an Angular Formly-related structure that can be used in a front-end (Angular) to render generic forms. <br>
- Run and access: http://localhost/api/model
