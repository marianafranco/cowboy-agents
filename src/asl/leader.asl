// Agent leader in project cowboy-agents

/* Initial beliefs and rules */
// I want to play "leader" in "leadership"
desired_role(leadership,leader).

// include common plans for MOISE+ agents
{ include("common.asl") }

/* Initial goals */

!create_group. // initial goal

// create a group to the team
+!create_group : true 
   <- //.send(orgManager, achieve, create_group(wpgroup)).
      jmoise.create_group(team,VAQUEIROS);
	  .print("Group ", "team" ," created");
	  jmoise.create_group(leadership,VAQUEIROS,VAQUEIROS);
	  .print("Sub-group ","leadership"," created");
	  jmoise.create_group(capture,VAQUEIROS,VAQUEIROS);
	  .print("Sub-group ","capture"," created");
	  jmoise.create_group(capture,VAQUEIROS,VAQUEIROS);
	  .print("Sub-group ","sabotage"," created").
-!create_group[error_msg(M),code(C),code_line(L)]
   <- .print("Error creating group, command: ",C,", line ",L,", message: ",M).


/* Organisational Events  */


/* Structural events */


/* Functional events */


/* Plans */
