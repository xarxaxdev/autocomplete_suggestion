import scala.collection.mutable.HashMap
import scala.collection.Map


//finite state automata
class FSA {
    case class state(nextState: HashMap[Character,state]) {   

        def isDefinedAt(char :Character): Boolean = {
            this.nextState.isDefinedAt(char)
        }       

        def addOne(char :Character, newState : state){
            this.nextState.addOne((char, newState))
        }       

        def get(char :Character): state = {
            this.nextState.get(char).getOrElse(new state(HashMap[Character,state]()))
        }       

        def print(prefix :String = ""){
            for ((k,v) <- this.nextState){
                printf("%s%s\n", prefix, k)
                v.print(prefix + "-")  
            }
                    
        }
    }
    var firstState = new state(HashMap[Character,state]())

    def print(){
        firstState.print("*")
    }

}
val myFSA = new FSA()
     
var firstState = myFSA.firstState;

//var FSA = new HashMap[Character,HashMap*]()//<String, HashMap>
def addWordToFSA(word : String){
        var lastState = firstState
            //println(lastState)
        for (c <- word){
            if (!(lastState.isDefinedAt(c))){
                //lastState = lastState + (c, new HashMap())
                lastState.addOne(c,new myFSA.state(new HashMap[Character,myFSA.state]()))
            }
            //println(myState)
            lastState = lastState.get(c)//.getOrElse(new state())
        }
}


val word = "some text"
addWordToFSA(word)
addWordToFSA("some titty")
//println(firstState )
myFSA.print()
