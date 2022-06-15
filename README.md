# Indexing
 
The system is designed to search words or sentences in which character. The source file is a JSON file that contains Play-id, Scene-id, Scene-Num, and text. Therefore, I have to import the given simple JSON file. Load the content to an Object by JSONParser with FileReader. Then convert the JSON Object to JSONArray. I have to get the array from the "corpus" header.

Next, I do the indexing process. This action will loop all the text from the JSON array content. Split the sentences into each term. Use the term to be key and store it on a map with the document id and position. If a word appears in a text multiple times, add the position in the position list by the same document id.

After the indexing process, I can do the Term-based Queries. Get all the document IDs and positions by term key. The size of the position means the term's appearance frequency in that document text. I can use a for loop to check all text. For example, which documents contain specific terms, and which documents have X terms more than Y terms. This helps me to solve the term.txt question.

In addition, I do the intersecting Posting Lists function. This function uses the given list of Posting-List. Using a while loop find the same document id posting list if the given list of posting list has next.

Find the approaching document id as a candidate. Move the pointer to that candidate number's posting from the list. Then compare the document id for the current posting list. If the current posting document id is the same, send them into the matching window function. If the return posting is not null, add the posting to postingList. Keep finding sentences until the list of PostingList ends. Final return the postingList.


The matching window function is used to find the sentence which exists in the text. Because the input posting list has the same document id, it means they are in the same text. For the process, Create a temp posting to save the first element position. Using a for loop on the first posting as starting word, the second for loop for the remaining posting list. Using the contain function because posting has a list to store the position. If exists a position is next to the beginning word. update the temp position and check the next postingList until all posting list have the right position. Add those positions into posting with document id. Finally use the termWrite function to sort the content and write the list of play id or scene id to the file. 

According to queries requirement, term 0.txt is very specific, the requirement shows that save the scene id when the "thee" or "thou" larger than "you". In the question, I think that or can represent a union of "thee" and "thou". Therefore, I add their frequency together as they have some doc id. After merging two maps, compare with you.
