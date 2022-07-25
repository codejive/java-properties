# Java Properties

[![](https://jitpack.io/v/jitpack/gradle-simple.svg?label=Release)](https://jitpack.io/#jitpack/gradle-simple)

Java Properties is a drop-in replacement of the ubiquitous
[`java.util.Properties`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Properties.html)
that everybody knows and loves (hates?).

It's an alternative implementation whose most important advantage over its standard Java sibling is that
it fully supports comments in input **and** output.

Meaning that a properties table like this can not only be read, but also created, changed
and be written back without losing any of the comments or formatting.

```properties
# Server configuration file

# Host name for remote server
remoteHost=myserver.example.com

# Port number
remotePort=8080

# Password hash algorithm to use
# (choose between MD5, SHA256 or SHA512)
passwordHash=SHA512
```
### Setup

Add it to your build file with:

```xml
<dependency>
    <groupId>org.codejive</groupId>
    <artifactId>java-properties</artifactId>
    <version>0.0.2</version>
</dependency>
```

or

```groovy
implementation 'org.codejive:java-properties:0.0.2'
```

or

```kotlin
implementation("org.codejive:java-properties:0.0.2")
```

And add this import to your code:

```java
import org.codejive.Properties;
```

### Usage

After which you can do something like:

```java
Properties p = new Properties();
p.setProperty("port", "8080");
p.setComment("port", "Port number to use for the server");
```

or just directly in a single line:

```java
Properties p = new Properties();
p.setProperty("port", "8080", "Port number to use for the server");
```

And if you would write this out:

```java
p.store(System.out)
```

you'll get:

```properties
# Port number to use for the server
port=8080
```

You can also set multi-line comments simply like this:

```java
p.setComment("port", "Port number to", "use for the server");
p.setProperty("port", "8080", "Port number to", "use for the server");
```

which would both result in:

```properties
# Port number to
# use for the server
port=8080
```

Retrieving values is simple:

```java
p.get("port"); // Returns "8080"
p.getProperty("port"); // Also returns "8080"
p.getComment("port") // Returns ["Port number to", "use for the server"]
```

### Comments

Comments are considered either "free" or "attached", which you could see as either being
just part of the file or attached to a property. For example:

```properties
# A header comment (free)

one=First value (that has no comment)

# Another free comment

# An attached comment
two=Second value
```

Any comments that directly precede a property, so no empty lines in between, are considered
"attached" to that property, which also means the comment can be retrieved using `props.getComment(key)`.
On the other hand "free" comments are not attached to anything and there's no way to retrieve their
values except when they are written out using `store()` or `list()`.

This is also the reason that, when using `store()` with a comment, eg. `props.store(out, "The first line")`,
it will actually insert an empty line after that comment, so it won't be considered attached to
the first property.

Another thing to take into account is that when retrieving comment, by using `getComment(key)`, you'll get a
list of strings that will include the comment character. So, for example in the table above, running
`getComment("two")` will return `"# An attached comment"`.

In the same way, when setting comments, either by using `setComment(key, comment)` or
`setProperty(key, value, comment)`, the comment lines are expected to start with a comment character.
Fortunately, it isn't an error to pass in lines that do not start with a comment character and the code will
try its best to figure out what comment character to use and prepend that to the lines.

### Compatibility

The `org.codejive.Properties` class is mostly a drop-in replacement of `java.util.Properties` with only
a couple of differences:

 - the API now uses `String` everywhere instead of having `Object` in certain places
 - the class does **not** extend `Hashtable`, it's a completely outdated class that shouldn't be used anymore
 - the `store()` methods do **not** write a timestamp at the top of the output
 - the `store()` methods **will** write an empty line between any comments at the top of the output and the actual data
