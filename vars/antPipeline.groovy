def call(Map vars, Closure body=null) {
   vars = vars ?: [:]
   def myParam = vars.get("myParam1", null)
   if (body)
   {
       body()
   }

}
