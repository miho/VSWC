# VSWC
[ ![Download](https://api.bintray.com/packages/miho/VRL-NeuroBox/vswc/images/download.svg) ](https://bintray.com/miho/VRL-NeuroBox/vswc/_latestVersion)

Reads and writes SWC files.

## How to Use VSWC (Sample Code)

```java
 // SWC sample
 String swcContent = ""
         + "# ********************************************* \n"
         + "# SCALE 1.0 1.0 1.0 \n"
         + "1 1 2.3   1.73   -0.12 12.61 -1\n"
         + "2 1 2.3   14.34  -0.13 12.62  1\n"
         + "3 2 62.3 -10.87  -0.14 12.63  1";
        
 System.out.println("----------------------------------------------------------");
 System.out.println("> original SWC data:");
 System.out.println("----------------------------------------------------------");
 System.out.println(swcContent);

 // read segments
 List<SWCSegment> segments = SWCSegment.fromString(swcContent);
        
 // TODO do something with the swc data

 // output segments
 System.out.println("----------------------------------------------------------");
 System.out.println("> segment data:");
 System.out.println("----------------------------------------------------------");
 segments.forEach(s -> System.out.println(s));
        
 // output segments in SWC format
 System.out.println("----------------------------------------------------------");
 System.out.println("> SWC data:");
 System.out.println("----------------------------------------------------------");
 String swcContent2 = SWCSegment.toSWCString(segments);
 System.out.println(swcContent2);
```

## How to Build VSWC

### Requirements

- Java >= 1.8
- Internet connection (dependencies are downloaded automatically)
- IDE: [Gradle](http://www.gradle.org/) Plugin (not necessary for command line usage)

### IDE

Open the `VSWC` [Gradle](http://www.gradle.org/) project in your favourite IDE (tested with NetBeans 8.2) and build it
by calling the `assemble` task.

### Command Line

Navigate to the [Gradle](http://www.gradle.org/) project (e.g., `path/to/VSWC`) and enter the following command

#### Bash (Linux/OS X/Cygwin/other Unix-like shell)

    bash gradlew assemble
    
#### Windows (CMD)

    gradlew assemble
