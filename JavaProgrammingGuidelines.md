# Java Programming Guidelines #



# Introduction #

Software development is a combination of art and engineering that requires analytical skills, creativity and discipline. The artist in us wants to have complete freedom of expression and the engineer in us wants to identify the single right way to do something and then have everyone do it the same way.

That's what makes programming guidelines and coding standards such a contentious topic.

However, anyone who's joined a project in mid-development, or taken over someone else's work, or had to come up to speed quickly on a large body of software knows that there is a real cost associated with everyone writing code their own way.

That's the purpose of these programming guidelines: to reduce cost by increasing consistency. These guidelines are written based on the understanding that there is often no single right way to do something. They are not intended to be a dogmatic statement of how to program. Rather the objective is to create consistency while reducing the likelihood of common problems.

These guidelines tend to be pragmatic, focused on programming in the real world, based on the assumption that if the product doesn't make it to market quickly it doesn't matter how elegantly it is coded and documented because it's probably going to die. This means there are some areas that favor speed over completeness. Although this approach is appropriate for rapid application development, it may be less so for well funded and well established projects.

# Variables #

Start variable names with a lowercase letter
  * `name`, `address`

Capitalize the first letter of each subsequent word
  * `phoneNumber`, `companyName`

Treat acronyms like words for the purpose of capitalization (RFP = Request for Proposal, POC = Proof of Concept, URL = Uniform Resource Locator)
  * `rfpPocUrl` instead of `RFPPOCURL`

Use nouns or noun phrases for variable names

Declare variables at first use, with narrowest possible scope

  * Local Variables: unique to each method invocation, independent of other invocations of same method
  * Parameters: link value in caller to variable in method, more on parameters below
  * Instance Variables (non-static fields): unique to each class instance, independent of other class instances
  * Class Variables (static fields): shared across all instances of the class, use sparingly unless final
  * Final Class Variables (constants): a named value that never changes (e.g. DEFAULT\_MTU, COMPILED\_PATTERN, DEFAULT\_SIMPLE\_DATE\_FORMAT)

Use getters / setters (encapsulation) for instance variables outside of the class or closely related hierarchies

# Parameters #

Design interfaces to minimize the need for parameter validation

  * Use Java enums for control parameters instead of numeric or string constants
  * Avoid unnecessary constraints

Validate parameters as early as possible and only when necessary

  * Public methods of classes that are likely to be used by other programmers
  * Parameters that accept a subset of the domain of the type (e.g. numeric ranges)
  * Parameters where there is likely to be confusion over what is valid

Otherwise avoid unnecessary parameter validation

  * Private methods, unless there are special validation concerns
  * Values that are likely to be caught quickly when used (e.g. `NullPointerException`, `IndexOutOfBoundsException`)
  * Components that are likely to be developed and maintained by a single programmer

Avoid changing the value of a parameter in a method, especially if the method is too long to comprehend at a glance or the assignment is not immediately obvious to **anyone** who must maintain the code

# Methods #

Start method names with a lowercase letter
  * `open`, `close`

Capitalize the first letter of each subsequent word
  * `addActionListener`

Treat acronyms like words for the purpose of capitalization

Use verbs or verb phrases for verb names

Remember that the object is often implicit (i.e. the instance) and does not need to be part of the method name

Use polymorphism to invoke the same action across different types
  * e.g. `object.toString()`

Use method overloading to invoke the same action with different numbers and types of arguments
  * Relatively safe when it differs by number of arguments

> `outputStream.write(byteValue)` versus `outputStream.write(buffer, offset, length)`

  * Less safe when the method signature differs only by the type of the parameter

> `myMethod(MyClass myClassInstance)` versus `myMethod(Serializable serializableValue)`

# Classes #

Start class names with an uppercase letter
  * `String`

Capitalize the first letter of each subsequent word
  * `StringBuilder`

Treat acronyms like words for the purpose of capitalization

Use categorizing nouns for class names

Consider using base class name in derived class names

Use the “is a” relationship to identify derivation

Use the “has a” relationship to identify containment

Use fields (class variables) for object attributes whose lifespan is the same as class instance lifespan

Use constructors to initialize fields, and as little else as possible

If a field's lifespan isn't the same as the class' lifespan, it may be a hint that you've got another class to implement, or that the field should actually be a method parameter or local variable

If a method repeatedly invokes methods on an instance of another class this may be a hint that the method is in the wrong class

# Constants #

Use all caps and underscores between words for constants (static final)
  * `LEFT_OFFSET`, `RIGHT_OFFSET`

Use prefixes or suffixes to group sets of constants

Treat acronyms like words

Always use static final constants for any literal that:
  * is used in more than one place
  * is likely to be changed over the development lifecycle
  * is not otherwise clear or immediately recognizable

Do not use static final constants for literals that:
  * require developers to do two levels of lookup to understand (e.g. if SQLCODES are specified as literal values in the documentation, it may be better to use those literal values directly in the code so that developers can immediately map between the documented value and its use. Introducing a constant means that the developer needs to flip back and forth to where the constant is defined, and means that the constant can get out of sync (e.g. misedited) with the documented value)

Consider using Java enums as an alternative

# More tips on names #

Do not abbreviate words in variable or class names unless the abbreviation is universally recognized

Do not use prefixes to describe variable type or scope

Restrict use of single character variable names
  * loop indexes (i, j)
  * well known quantities (x, y for geometry or e for exception)

Include the class name in the variable name if it helps

If the “same” quantity is stored in two different variables then the name should clearly identify the difference
  * `inputString`, `inputInteger`

Likewise, if the same class is used for two similar variables then the name should identify the difference
  * `xmlSourceString`, `xslSourceString`

Use correct spellings

# Formatting #

Line up the opening and closing brackets in the same column. This is a contentious topic amongst Java programmers. See http://en.wikipedia.org/wiki/Indent_style#Allman_style for more information.

Use separate blocks, surrounded by opening and closing brackets, for then and else clauses. Never place a then or else clause on the same line as the condition.

Favor simplicity over complexity.

In linguistics, the term "center embedding" refers to the process of embedding a phrase in the middle of another phrase of the same type. This often leads to difficulty with parsing that would be difficult to explain on grammatical grounds alone. The most frequently used example involves embedding a relative clause inside another one, as in:

  * A man that a woman loves
  * A man that a woman that a child knows loves
  * A man that a woman that a child that a bird saw knows loves
  * A man that a woman that a child that a bird that I heard saw knows loves

Likewise, in software development, it’s possible to pack a lot of content into a single expression, statement or method... but that doesn’t mean you should!

  * Minimize use of nested expressions (assign intermediate values to variables to help document what's going on)
  * Minimize use of nested function calls (assign intermediate function returns values to make it easier to step into function when debugging)
  * Minimize use of nested block statements (factor into methods to document the block's purpose and avoid future confusion)
  * Minimize use of nested anonymous classes (factor into nested class to avoid confusion over temporal aspects of definition versus use)
  * Minimize number of statements per method (the entire method should be visible on a single page in a typical editor)
  * Avoid multiple return statements, but favor them over convoluted logic

# Exceptions #

Java has two broad categories of exceptions
  * Checked exceptions – part of method signature
  * Runtime exceptions – not part of method signature

Checked exceptions seem like a great idea in theory
  * Ensure that exceptions are documented and handled by caller

However, in practice checked exceptions quickly pollute and overwhelm everything they come in contact with

Here is how to use Java exceptions so that those who have to invoke or maintain your code don’t hate you:
  * Throw only instances of exceptions derived from `RuntimeException`
  * Reuse standard exceptions (e.g `IllegalArgumentException` and `IllegalStateException`) when possible
  * Build hierarchies derived from `RuntimeException` if necessary
  * Do not throw checked exceptions, especially beyond "your" code
  * Catch checked exceptions thrown by the Java SDK and others and rethrow as runtime exceptions
  * When implementing a dispatcher or other construct where developers would reasonably expect runtime exceptions to be caught and handled gracefully, do so
  * Do not declare a method as throwing Exception, just to avoid handling checked exceptions. This just shifts the burden to someone else. The further away the exception gets, the less likely anything useful can be done with it.

As Josh Bloch says,
> The burden (of checked exceptions) is justified if the exceptional condition cannot be prevented by proper use of the API _and_ the programmer using the API can take some useful action once confronted with the exception. Unless both of these conditions hold, an unchecked exception is more appropriate.

The worst examples of checked exceptions are in the Java I/O and JDBC packages.

# Try, Catch and Finally #

Just because it is possible to include a finally clause in a try / catch block, doesn't mean that it's a good idea.

Generally the scope of a finally clause is different from the scope of a try / catch block.

For example, when acquiring a resource that must be released, immediately follow the acquisition of the resource with a try / finally block, regardless of whether a try / catch block is required somewhere else in the method.

This is especially true in methods that must acquire multiple resources that must be released. Each resource should be protected by its own finally block.

This results in a nested structure of try / finally blocks and ensures that resources are released in the narrowest possible scope. It also eliminates the need to create variables with broad scope that are initialized to null outside a try / catch / finally block and then checked against null in a finally block (in case execution hadn't yet reached the point of acquisition).

Finally, it may be possible to surround the (possibly nested) try / finally blocks with a single set of try / catch blocks (e.g. at the method level). This eliminates the need for separate try / catch blocks in each of the finally blocks, although it may result in obscuring the first instance of an exception by throwing another instance from the finally block.

Here is an example:

```

try
{
  Resource1 resource1 = acquireResource1();
  try
  {
    Resource2 resource2 = acquireResource2();
    try
    {
      resource2.doSomething(resource1);
    }
    finally
    {
      resource2.release();
    }
  }
  finally
  {
    resource1.release();
  }
}
catch (CheckedException1 e) // thrown by acquireResource1, acquireResource2 and doSomething
{
  throw new RuntimeException(e);
}
catch (CheckedException2 e) // thrown by acquireResource1, acquireResource2 and doSomething
{
  throw new RuntimeException(e);
}

```

# Exceptions and Logging #

When implementing a high level dispatcher or similar construct catch and log all exceptions, including the stack trace.

Unless a method can supply additional information that is important in diagnosing a problem:

  * Avoid unnecessary catching and chaining of exceptions. Each link in the chain makes it harder to read the stack trace and easier to miss something.

  * Avoid unnecessary exception catching and logging. It clutters the code, makes it harder to understand the algorithm and may actually increase the diagnostic burden.

# Refactoring #

Refactor common code that consists of more than two statements into a common method

Keep variable, class and method names consistent

Keep argument list parameter order consistent

If it doesn't work, delete it, and try a new approach, don't just add more code to make it work

# Eclipse Best Practices #

Source Menu
  * Add / Remove Comments
  * Shift Right / Left
  * Format (Ctrl+Shift+F)
  * Organize Imports (Ctrl+Shift+O)
  * Override / Implement Methods
  * Generate Getters and Setters
  * Generate Delegate Methods
  * Generate hashCode() and equals()
  * Generate Constructors using Fields

Refactor Menu
  * Rename
  * Move
  * Change Method Signature (Alt+Shift+C)
  * Extract Method (Alt+Shift+M)
  * Extract Local Variable (Alt-Shift+L)
  * Convert Local Variable to Field
  * Extract Constant
  * Inline
  * Push Down / Pull Up
  * Encapsulate Field
  * Ctrl+1 Menu
  * Search Menu (e.g. Show References, Ctrl+Shift+G)

# Comments #

JavaDoc is required for API's intended for general use:
  * public classes
  * public and protected methods
  * public instance variables (fields)

JavaDoc is great. Java would never have achieved its level of popularity without it. However, there are some considerations.

There is a cost to JavaDoc, especially during rapid prototyping, when the code is undergoing frequent refactoring, or being developed and used by just one programmer. There's nothing quite like the feeling of spending valuable time writing the JavaDoc for a method, only to realize later that the method itself is no longer necessary.

JavaDoc should not discourage refactoring. If every method has to have JavaDoc, then there may be a tendency to create fewer methods. It's much more important to create well modularized, well named methods than it is to write JavaDoc for every one of them.

JavaDoc should not just restate what is obvious from well chosen class, method and variable names. If it's not possible to say more in the JavaDoc than what is already obvious from the names, there is no need to write the JavaDoc. The worst example of this is the Eclipse JavaDoc template for getters and setters.

When writing JavaDoc, it is important to distinguish between users of the documented entity and implementors of the documented entity. By default, the JavaDoc should be focused on users. Implementation notes should be clearly labeled as such.

# Redundant Initialization #

The Java compiler does a great job of detecting possible uninitialized variable references. For this reason, when doing conditional initialization of a variable and evaluating all branches, do not perform unnecessary initialization in the declaration, it will prevent the compiler from detecting a possible logic error. In the following example, the Java compiler can determine that myClassInstance may not be initialized because of the missing else. It would not be able to do so if myClassInstance had been initialized to null in the declaration.

```

MyClass myClassInstance;

if (myVariable.isOneThing())
{
  if (anotherVariable.isAnotherThing())
  {
    myClassInstance = getValue1();
  }
  else
  {
    myClassInstance = getValue2();
  }
}
else
{
  if (myVariable.isAnotherThing())
  {
    myClassInstance = getValue3();
  }
}

myClassInstance.doSomething();

```

Similarly, the Java specification states that instance variables (fields) are initialized to zero or null. Therefore it is not necessary to explicitly initialize them to these values. Doing so makes it more difficult to step into the constructor using a debugger.

# Final Thoughts #

Be Consistent - Although Emerson said "a foolish consistency is the hobgoblin of little minds," it is much easier to be consistent than it is to distinguish between wise and foolish consistency. Anyone (including yourself) who has to maintain your code will undoubtedly find your consistency quite wise.

Eliminate complexity - In software development, complexity creeps in everywhere, and it takes active effort to reduce it. Complex code is hard to understand and maintain. Some programmers equate complexity with skill, when in fact just the opposite is true. As Einstein said, "Make everything as simple as possible, but not simpler.“

And most of all: refactor, refactor, refactor!