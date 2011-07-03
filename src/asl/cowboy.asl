// Agent cowboy in project cowboy-agents

/* Initial beliefs and rules */
// I want to play "captor" in "capture"
desired_role(capture,captor).

// I want to commit to "m1" mission in "catchCows" schemes
desired_mission(catchCowScheme,m1).

// include common plans for MOISE+ agents
{ include("common.asl") }
{ include("moving.asl") }

/* Initial goals */
!connect_to_server.	// I want to connect to the server


/* Plans */

+sim_start(SimId): true
	<- .print("Simulation started").


// goal: herding cows
{ begin maintenance_goal("+pos(_,_,_)") }

+!herding_cows
	: pos(X,Y,ActionId) & target(TX,TY)
	<-  //.print("[herding_cows1]");
		.drop_desire(move);
		!move.

+!herding_cows : pos(X,Y,ActionId) & target(X,Y).
	//<- 	//.print("[herding_cows2]");
		//.drop_desire(move);
		//!move.

/*
+!herding_cows : target(TX,TY)
	<-	//.print("[herding_cows3]");
		.drop_desire(move);
		!move.
*/

+!herding_cows : pos(X,Y,ActionId) &
	jia.near_least_visited(X,Y,ToX,ToY)
	<- 	//.print("[herding_cows4]");
		!pos(ToX,ToY).

+!herding_cows <- .print("[herding_cows] do nothing").
{ end }