

### GENERAL CONSIDERATIONS
The implementation for this uses a DFA, that tries to recognize words and returns the state it reaches after processing a word (whether it accepts it or doesn't, and if there are other words that match that prefix). Reasons being:
- Cost constant time * l, where l is the length of the prefix(basically constant)
- Easy to add new words
- Easy to remove words
For those reasons I think it solves the detailed nicely.

My DFA structure basically consists of States, where each node has a hashmap<char,state>. My reasoning with this is to have pretty much constant time on all cases(except the exponentially uncommon unlucky). Since the access time is generally constant in access time for hashmaps, and traversing each step is constant. 


#Limitations
This is a very simple and memory-happy test. I would not trust this code for a real-life system that will grow, since relying on scalability for memory is a dangerous path to take. If I had to implement this from 0 for an ever-growing system, disk-usage solutions would be necessary, with all that accompanies those(cacheing policies, etc). You would also need some horizontal partitioning as well(sharding data, a couple of replica servers, etc).

I would also realistically use a preexisting solution for this, like Elasticsearch that can also cover tangential future needs(term searching and other text-specialized functions).  

#Other considerations

There exist way to minimize the DFA build after inputting all the words. https://en.wikipedia.org/wiki/DFA_minimization. However, for the sake of simplicity I did not implement them in this test. The loss here would going from a fully arborescent structure to joining some of the subtrees to remove redundancy. It would be a net gain given the problem that we have, so I would implement it.

With the current implementation, you could also add another function to get the amount of responses given a prefix, and it would be a certainly valuable function. 

A radix tree would be a interesting improvement. On my very basic implementation of a DFA it would be a very welcome improvemnt, with no added costs(inserting is about the same or less, search is faster) that would just require some logic to be able to merge states. My main concern in this is how leaving our DFA as a radix tree will affect the DFA minimization algorithm. The smart choice here would probably be something along these lines:
- Work with radix tree (use on production)
- Periodically, transform this radix tree(in a non-production environment), make it back to being a normal char to char search instead of using prefixes. Run the DFA minimization algorithm on the un-radixed tree. Turn it into a radix tree again and return it to production.
My other concern with a radix tree would be the collisions in the hashmaps, which may affect performance. This of course is dependant on the hash function, but having many prefixes may be detrimental for the node's Hashmap.


As shown in the provided examples, I use A DFS search after matching a prefix, but implemented BFS as well to get a bit more familiarized with Scala.

For this test I also considered a TST better than a Trie, since the cases where we'll want to check all the edges are very few.

###ELASTIC SEARCH APPLIED TO THIS SOLUTION

In order to perform searches like our program does, we would want to use the "keyword" specification for each word we index. If we want to have the data a bit more partitioned, I would recommend using a different index for each language. Even better than that would be one for each physical region to avoid concept drifting/local differences. To give an example I know about, the word "carro" in Spain would never be used to refer to a car, but in Mexico you would. Locality is the the way to keep it simple usually.

As in any text analyzing pipeline, there are several concepts to take into account:
- Tokenization: in our worbase(the one in the test) there are several examples of words split by " ". Depending on what we want to consider we have other options to choose https://www.elastic.co/guide/en/elasticsearch/reference/6.8/analysis-tokenizers.html. For the examples provided the "standard" tokenizer(which is the default) would work, but we may want "letter" or something else, I'd need a larger test sample. 
- Stopwords: In my experience, you generally want to filter out the stopwords, and unless in this case we really want to know the original indexed term I would do so.
- Stemming/Lemmatization: These, while they can be interesting for some text AI analysis would not be too useful for the current exercise.  


###WHAT WOULD YOU DO IF THE WORDBASE WAS MUCH LARGER (300GB) ? 

As mentioned earlier, you would want a radix tree that you DFA-minimize from time to time. 
We would definitely need a disk-intensive solution and a solution that can work in a cluster:
In order to keep this scalable, we may want to keep the nodes from the DFA separate and bulk them when they have a similar prefix. Each chunk of similar nodes should be stored close by, then have a master node(in a cluster), which holds a reference to it. 


###WHAT WOULD YOU DO TO MATCH WORDS WITH INFIXES("PRO" IN "REPROBE")?
The base implementation would need some minor alterations, mainly a BFS search would have to be done from the first state. Other than that it is rather straightforward, and worth noting that these searches could easily be done in parallel. This would be CPU intensive, but not an issue on any other side.

