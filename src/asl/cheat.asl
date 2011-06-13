// Agent cheat in project cowboy-agents

/* Initial beliefs and rules */
// I want to play the role "cheat" in the group "sabotage"
desired_role(sabotage,cheat).

// I want to commit to mission "m101" in "disruptEnemyScheme" scheme
desired_mission(disruptEnemyScheme,m101).

// include common plans for MOISE+ agents
{ include("common.asl") }
{ include("moving.asl") }

/* Initial goals */

!connect_to_server.	// I want to connect to the server

/* Organisational Events  */


/* Structural events */


/* Functional events */


/* Plans */
// Plans previously defined by the architecture
+!g11[scheme(Sch)]
	: true
	<- .print("Goal g11 satisfied! I've located an enemy's fence switch.");
		jmoise.set_goal_state(Sch,g11,satisfied).

+!g121[scheme(Sch)]
	: true
	<- .print("Goal g121 satisfied! I've opened the enemy's fence switch and am keeping it open.");
		jmoise.set_goal_state(Sch,g121,satisfied).

+!g1222[scheme(Sch)]
	: true
	<- .print("Goal g1222 satisfied! I've walked through the fence.");
		jmoise.set_goal_state(Sch,g1222,satisfied).

// behavior definition for simulation start 
+sim_start(SimId): true
	<- .print("Simulation started");
	!move_to_next_fence(next_fence).

// move to next enemy's fence plan
+!move_to_next_fence(next_fence)
	: pos(X,Y,ActionId)
	<- 
	jia.next_fence(X,Y,ToX,ToY, D);
	//.print("Moving to the enemy's fence");
	do(D,ActionId);
	jia.sleep_some_time;
	!!move_to_next_fence(next_fence).

+!move_to_next_fence(next_fence)
	: not pos(_,_,_)
	<- //.print("Almost moving"); 
	jia.sleep_some_time;
	 !move_to_next_fence(next_fence).