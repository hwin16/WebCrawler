val a = <unit><if></if><cpp:if xmlns:cpp="http://www.sdml.info/srcML/cpp" xmlns="http://www.sdml.info/srcML/src">#<cpp:directive>if</cpp:directive> <expr><name>ENABLE_SH_MATH_SUPPORT</name></expr></cpp:if></unit>
val b = a \\ "if" filter(z => z.namespace == "http://www.sdml.info/srcML/cpp")
val cmd = "ls -al" !