// Agent leader in project cowboy-agents

/* Initial beliefs and rules */
// I want to play "leader" in "capture"
desired_role(capture,leader).

// I want to commit to "m2" mission in "catchCows" schemes
desired_mission(catchCowScheme,m2).

// include common plans for MOISE+ agents
{ include("common.asl") }
{ include("moving.asl") }

/* Initial goals */

!create_group. // initial goal
!connect_to_server.	// I want to connect to the server

// create a group to the team
+!create_group : true 
   <- //.send(orgManager, achieve, create_group(wpgroup)).
      jmoise.create_group(gr_name(team),team);
	  .print("Group ", "team" ," created");
	  jmoise.create_group(capture,team);
	  .print("Sub-group ","capture"," created");
	  jmoise.create_group(sabotage,team);
	  .print("Sub-group ","sabotage"," created").
-!create_group[error_msg(M),code(C),code_line(L)]
   <- .print("Error creating group, command: ",C,", line ",L,", message: ",M).


/* Organisational Events  */

/* Structural events */
// when I start playing the role "leader",
// create a catchCow scheme
/*
+play(Me,leader,GId)
	: .my_name(Me)
	<- jmoise.create_scheme(catchCowScheme,[GId]);
	.print("Scheme created").
*/	

/* Functional events */
// when a scheme has finished, start another
/*
-scheme(catchCowScheme,_)
	: group(team,GId)
	<- jmoise.create_scheme(catchCowScheme, [GId], SchId);
	.print("Scheme ",SchId," created").
*/

/* Plans */
// when authenticated and playing role "leader",
// create a catchCow scheme 
+auth(ok): play(Me,leader,GId) & .my_name(Me)
	<- jmoise.create_scheme(catchCowScheme,[GId]);
	.print("Scheme created").
	
+sim_start(SimId): true
	<- .print("Simulation started");
	!search_cow(near_unvisited).

/*
+!g1[scheme(Sch)] : true
	<- .print("Goal g1 satisfied!");
		jmoise.set_goal_state(Sch,g1,satisfied).

+!g22[scheme(Sch)] : true
	<- .print("Goal g22 satisfied!");
		jmoise.set_goal_state(Sch,g22,satisfied).
   
+!g31[scheme(Sch)] : true
	<- .print("Goal g31 satisfied!");
		jmoise.set_goal_state(Sch,g31,satisfied).
   
+!g4[scheme(Sch)] : true
	<- .print("Goal g4 satisfied!");
		jmoise.set_goal_state(Sch,g4,satisfied).
   
+!g5[scheme(Sch)] : true
	<- .print("Goal g5 satisfied!");
		jmoise.set_goal_state(Sch,g5,satisfied).
   
+!g6[scheme(Sch)] : true
	<- .print("Goal g6 satisfied!");
		jmoise.set_goal_state(Sch,g1,satisfied).
*/