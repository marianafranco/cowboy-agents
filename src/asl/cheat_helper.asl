// Agent cheat in project cowboy-agents

/* Initial beliefs and rules */
// I want to play the role "cheat" in the group "sabotage"
desired_role(sabotage,cheat_helper).

// I want to commit to mission "m102" in "disruptEnemyScheme" scheme
desired_mission(disruptEnemyScheme,m102).

// include common plans for MOISE+ agents
{ include("common.asl") }
{ include("moving.asl") }

/* Initial goals */

!connect_to_server.	// I want to connect to the server

/* Organisational Events  */


/* Structural events */


/* Functional events */


/* Plans */

// behavior definition for simulation start 
+sim_start(SimId): true
	<- .print("Simulation started");
	!helper_move_to_next_fence(next_fence).

// move to next enemy's fence plan
+!helper_move_to_next_fence(next_fence)
	: pos(X,Y,ActionId)
	<- 
	jia.helper_next_fence(X,Y,ToX,ToY, D);
	//.print("Moving to the enemy's fence");
	do(D,ActionId);
	jia.sleep_some_time;
	!!helper_move_to_next_fence(next_fence).

+!helper_move_to_next_fence(next_fence)
	: not pos(_,_,_)
	<- //.print("Almost moving"); 
	jia.sleep_some_time;
	 !helper_move_to_next_fence(next_fence).