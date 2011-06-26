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
   <- jmoise.create_group(gr_name(team),team);
	  .print("Group ", "team" ," created");
	  jmoise.create_group(capture,team);
	  .print("Sub-group ","capture"," created");
	  jmoise.create_group(sabotage,team);
	  .print("Sub-group ","sabotage"," created").
-!create_group[error_msg(M),code(C),code_line(L)]
   <- .print("Error creating group, command: ",C,", line ",L,", message: ",M).


/* Organisational Events  */

/* Structural events */	

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
	<- .print("Simulation started").


// goal: coordinate cowboys
{ begin maintenance_goal("+pos(_,_,_)") }

+!coordinate_cowboys : pos(X,Y,_) & .my_name(Me) & scheme_group(Sch,G) &
	not .intend(coordinate_cowboys) &
	jia.preferable_cluster(X,Y,L,S,N) &
	.list(L) &
	.length(L) > 0 &
	L = [pos(ClX,ClY),_] &
	.count(play(_,captor,G),NAg) &
	jia.position_to_cluster(ClX,ClY,NAg,Formation)
  	<-	.findall(P, play(P,captor,G), Agents);
  	   	.print("Formation is: ",Formation," and Agents are: ",Agents);
  	   	!send_target(Agents,Formation).

/* mudar para ir para a cerca
+!coordinate_cowboys : pos(X,Y,_) & jia.near_least_visited(X,Y,ToX,ToY) &
	not .intend(pos(_,_)) 
   	<-  !pos(ToX,ToY).
*/

+!coordinate_cowboys.

-!coordinate_cowboys[error_msg(M),code(C),code_line(L)]
	<- .print("Error on coordinate_cowboys, command: ",C,", line ",L,", message: ",M).

{ end }

+!send_target(Agents,[pos(X,Y)|TLoc])
 	<- !find_closest(Agents,pos(X,Y),NearAg);
 	   .send(NearAg,tell,target(X,Y));
 	   .delete(NearAg,Agents,TAg);
 	   !send_target(TAg,TLoc).

+!send_target([],[]).
 
+!find_closest(Agents, pos(FX,FY), NearAg) // find the agent near to pos(X,Y)
 	<- .my_name(Me);
 	   .findall(d(D,Ag),
           .member(Ag,Agents) & ally_pos(Ag,AgX,AgY) & jia.path_length(FX,FY,AgX,AgY,D),
                            Distances);
           .min(Distances,d(_,NearAg)).
-!find_closest[error_msg(M),code(C),code_line(L)]
	<- .print("Error on find_closest, command: ",C,", line ",L,", message: ",M).

// goal: open fence
{ begin maintenance_goal("+pos(_,_,_)") }
+!go_open_fence
	<-  .print("go open fence").
{ end }