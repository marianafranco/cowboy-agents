// Agent cowboy in project cowboy-agents

/* Initial beliefs and rules */
// I want to play "cowboy" in "sabotage"
desired_role(capture,captor).

// I want to commit to "m1" mission in "catchCows" schemes
desired_mission(catchCowScheme,m1).

// include common plans for MOISE+ agents
{ include("common.asl") }

/* Initial goals */

!connect_to_server.	// I want to connect to the server

/* Organisational Events  */


/* Structural events */


/* Functional events */


/* Plans */
+!g1[scheme(Sch)] : true
	<- .print("Goal g1 satisfied!");
		jmoise.set_goal_state(Sch,g1,satisfied).

+!g21[scheme(Sch)] : true
	<- .print("Goal g21 satisfied!");
		jmoise.set_goal_state(Sch,g21,satisfied).
		
+!g32[scheme(Sch)] : true
	<- .print("Goal g32 satisfied!");
		jmoise.set_goal_state(Sch,g32,satisfied).

+!g4[scheme(Sch)] : true
	<- .print("Goal g4 satisfied!");
		jmoise.set_goal_state(Sch,g4,satisfied).