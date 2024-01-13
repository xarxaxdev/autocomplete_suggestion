import scala.io.StdIn.readLine
import scala.io.Source
import scala.collection.mutable.HashMap
//Used in the BFS, technically not used in the final solution
import scala.collection.mutable.Queue



//we create a finite state automata
//We basically create a directed graph where each node is a state
//states can be accepting or non-accepting
//a character is used to traverse to the next node 
//TODO: Add a DFA minimization algorithm that could be called
class FSA {
    case class State(nextState: HashMap[Character,State]) {   
    
    	//Determines whether the current state finishes a word
		var isAcceptingState = false;
		
		//STATE ALTERING FUNCTIONS
		
        //Setter for the final word status, for when we want to add/remove words 
        def setFinishedWord(bool :Boolean){
            this.isAcceptingState = bool;
        }
              	
		//Adds a new state given a certain character 
        def addOne(char :Character, newState : State){
            this.nextState.addOne((char, newState))
        }       
              

              
              
        // EXPLORING FUNCTIONS
              
       	//Checks if word is final		
		def isWord(): Boolean = {
            this.isAcceptingState;
        }      
              
        //Checks if a character is usable to go to a next State      
        def isDefinedAt(char :Character): Boolean = {
            this.nextState.isDefinedAt(char)
        }       
        
		//Returns the next State given a certain character
        def get(char :Character): State = {
            this.nextState.get(char).getOrElse(new State(HashMap[Character,State]()))
        }       


		//returns the amount of states accessible from this State
        def length(): Int = {
            this.nextState.size
        }       
        
        //returns the possible character to traverse from this State
        def getNextStates(): Array[Character] = {
            this.nextState.keys.toArray
        }       

		
		// UTILITY FUNCTIONS
   		
   		//a very simple printer made to have general idea of the topography
        def print(prefix :String = ""){
            for ((k,v) <- this.nextState){
                printf("%s%s\n", prefix, k)
                v.print(prefix + "-")  
            }
                    
        }
    }
    
    //root node in our directed graph
    var firstState = new State(HashMap[Character,State]())



	//BUILD FUNCTIONS
	
	//This function adds all the states for a new word to our acceptor
    def addWordToFSA(word : String){
            var lastState = firstState
            for (c <- word){
                if (!(lastState.isDefinedAt(c))){
                    lastState.addOne(c,new State(new HashMap[Character,State]()))
                }
                lastState = lastState.get(c)
            }
            lastState.setFinishedWord(true)
    }


	//EXPLORING FUNCTIONS
	
	//Given a state and prefix, this function tries to consume that prefix 
	//using each char to navigate to a next state
	//if unable to match that prefix, it will return None
    def prefixMatching(prefix: String, lastState :State):Option[State] = {
        if (prefix.length() == 0)//Successfully matched all the prefix
            Some(lastState)
        else {
            val c = prefix.charAt(0)
            if (lastState.isDefinedAt(c)){//Process the next character
                prefixMatching( prefix.takeRight(prefix.length()-1), lastState.get(c))
            } else None     //Unsuccessful at matching
        }
    }
    
	//DFS search to solve the main search problem.
	//given a state, finds N accepting words from that state if possible,
	//if not will return the ones found
    def getWordsDFS(lastState :State, numWords: Int = 4): Array[String] = {
        var res = Array[String]()
	    if (lastState.isWord){//if we are on an already accepting state it is part of the solution
    		res = res :+ ""
	    }
	    val nextStates = lastState.getNextStates().sorted
	    nextStates.map(char => {//we search in the next states
	    	if  (res.length < numWords)// if we have enough results we don't keep searching
	    		res = res ++ getWordsDFS(lastState.get(char), numWords - res.length).map(x=>char + x)
	   	})		//we keep track of the chars so that we can recover the whole word

    	res
    }


	//(technically not used in the final solution)
	//BFS search to solve the main search problem.
	//given a state, finds N accepting words from that state if possible,
	//if not will return the ones found
    def getWordsBFS(lastState :State, numWords: Int = 4): Array[String] = {
        var res = Array[String]()
		var myQ = Queue[(String,FSA.this.State)](("",lastState));	
		while(!myQ.isEmpty) {
			val (prefix, curState) = myQ.front;//check latest state
			myQ.dequeue()
	        //we add the next states to the queue
	        curState.getNextStates().sorted.map(x => myQ.enqueue((prefix+x,curState.get(x))))
	        if (curState.isWord){//if we get a match we update our solution
	        	res = res :+ prefix
    			if (res.length == numWords) //if we're finished we want to empty our search queue
					myQ = Queue[(String,FSA.this.State)]();	
	        }
    	}
    	res
    }


	//Given a certain prefix, returns 4 indexed words that match it
    def getWordRecommendation(prefix: String): Array[String] = {
    	//we first get the state where we fully matched the prefix
        val maxPrefixMatch = prefixMatching(prefix, firstState)
        //then we find words indexed
        maxPrefixMatch match {
            case Some(state) =>
                getWordsDFS(state,4).map(x => prefix+ x)
            case None =>
                new Array[String](0)        
        }
    }
    
    
	// UTILITY FUNCTIONS

	//very basic print function
    def print(){
        firstState.print("*")
    }
    
    
}




object InputHandler {
    //finite state automata
    val myFSA = new FSA()

    def loadWordbase(filename : String = "wordbase.txt"){
        for (line <- Source.fromFile(filename).getLines) {
            myFSA.addWordToFSA(line)
        }
    }



    def main(args: Array[String]){
    	//load our data
        loadWordbase()
        do {//infinitely read input so that we can do several searches
            var inputString = readLine
            printf("---You wrote %s---\n", inputString) 
            var words = myFSA.getWordRecommendation(inputString)    
            println(words.mkString("\n"))   
        }   while (true)
    }
}


