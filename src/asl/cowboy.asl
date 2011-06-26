// Agent cowboy in project cowboy-agents

/* Initial beliefs and rules */
// I want to play "cowboy" in "sabotage"
desired_role(capture,captor).

// I want to commit to "m1" mission in "catchCows" schemes
desired_mission(catchCowScheme,m1).

// include common plans for MOISE+ agents
{ include("common.asl") }
{ include("moving.asl") }

/* Initial goals */

!connect_to_server.	// I want to connect to the server

/* Organisational Events  */


/* Structural events */


/* Functional events */


/* Plans */

+sim_start(SimId): true
	<- .print("Simulation started").

// goal: herding cows
{ begin maintenance_goal("+pos(_,_,_)") }

+!herding_cows : .intend(search_cow)
	<-  .drop_desire(search_cow).

+!herding_cows : pos(X,Y,_) & not target(TX,TY) &
	jia.near_least_visited(X,Y,ToX,ToY)
	<- 	.drop_desire(search_cow);
		!pos(ToX,ToY).

+!herding_cows : target(TX,TY)
	<- 	.drop_desire(move);
		!move.

+!herding_cows.
{ end }