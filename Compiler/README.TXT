Parser ReadMe:

In order to change the grammer simply drag the new Grammar into the Compiler folder in eclipse. It is important to drag it into the folder and not the src folder.

In order to change the testFile do the same. I hard coded the fileNames so please follow GrammarList.txt and testFile.txt


I noticed that the SLR table generated for the grammer is not an SLR unless i am missing something. in state 102 there is a reduction on the follow of Ss as well as a shift to state 104 on else. Since else is in the follow of Ss this creates a conflict. I hardcoded removing that case and the parser worked.  
