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
	: .intend(search_cow) &
	  pos(X,Y,ActionId) & target(TX,TY)
	<-  .drop_desire(search_cow);
		.drop_desire(move);
		!move.

+!herding_cows : pos(X,Y,ActionId) & not target(TX,TY) &
	jia.near_least_visited(X,Y,ToX,ToY) & not .intend(search_cow)
	<- 	!pos(ToX,ToY).

+!herding_cows : pos(X,Y,ActionId) & target(X,Y)
	<- 	//.drop_desire(move);
		//!move.
		.drop_desire(move);
		!move_to_corral.

+!herding_cows : target(TX,TY)
	<-	.drop_desire(move);
		!move.

+!herding_cows.
{ end }