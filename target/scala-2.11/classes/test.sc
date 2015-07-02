import java.util.HashMap

val list = List("a1", "a2", "a3", "b1", "b2", "b3")
val map = new HashMap[String, Integer]()
for (e <- list){
  if (map.containsKey(e)){
    val old = map.get(e)
    map.put(e, old + 1)
  }
  else {
    map.put(e, 1)
  }
}