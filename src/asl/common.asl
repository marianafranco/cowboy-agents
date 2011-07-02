// ******************************************************** //
// Common plans for organised agents based on MOISE+ model. //
// ******************************************************** //

// These plans use the beliefs:
// . desired_role(<GrSpec>,<Role>) and
// . desired_mission(<SchSpec>,<Mission>).

/* Organisational Events */

/* Structural events */
// when a group is created and I desire to play in it,
// adopts a role
+group(GrSpec,Id)
	: desired_role(GrSpec,Role)
	<-  jmoise.adopt_role(Role,Id);
		.print(Role," on group ",GrSpec," created").

/* Functional events */
// finish the scheme if it has no more players
// and it was created by me
/*
+sch_players(Sch,0)
: .my_name(Me) & scheme(_, Sch)[owner(Me)]
<- jmoise.remove_scheme(Sch).
*/

/* Deontic events */
// when I have an obligation or permission to a mission
// and I desire it, commit
+obligation(Sch, Mission)
	: scheme(SchSpec,Sch) & desired_mission(SchSpec, Mission)
	<- jmoise.commit_mission(Mission,Sch).
+permission(Sch, Mission)
	: scheme(SchSpec,Sch) & desired_mission(SchSpec, Mission)
	<- jmoise.commit_mission(Mission,Sch).

// when the root goal of the scheme is achieved,
// remove my missions and the scheme
+goal_state(Sch, _[root], achieved)
	<-  jmoise.remove_mission(Sch);
		.my_name(Me);
		if (scheme(_,Sch)[owner(Me)]) {
			if (not sch_players(Sch,0)) {
				.wait( { +sch_players(Sch,0)} , 1000, _)
			};
			jmoise.remove_scheme(Sch)
		}.

// if some scheme is finished, drop all intentions related to it.
-scheme(_Spec,Id)
	<- .drop_desire(_[scheme(Id)]).

+error(M)[source(orgManager)]
	<- .print("Error in organisational action: ",M); -error(M)[source(orgManager)].


// ********************************************** //
// Common plans for all the agents on the system. //
// ********************************************** //

// connects to the server
+!connect_to_server
	<-  connectToServer;
		!authenticate_to_server.
-!connect_to_server[error_msg(M),code(C),code_line(L)]
   <- .print("Error when connecting to server, command: ",C,", line ",L,", message: ",M).

// authenticates the agent to the server
+!authenticate_to_server
	<- authentication.
-!authenticate_to_server[error_msg(M),code(C),code_line(L)]
   <- .print("Error authenticating to the server, command: ",C,", line ",L,", message: ",M).

// corral location perception
+corral(UpperLeftX,UpperLeftY,DownRightX,DownRightY)
  <- -+corral_center((UpperLeftX + DownRightX)/2, (UpperLeftY + DownRightY)/2).


// goal: search cow
/* 
{ begin maintenance_goal("+pos(_,_,_)") }

+!search_cow
   	: pos(X,Y,ActionId) & not jia.found_cow &
      jia.near_least_visited(X,Y,ToX,ToY) &
      not .intend(pos(_,_)) & not target(TX,TY)
   	<-  !pos(ToX,ToY).

+!search_cow : not pos(_,_,_) & not target(TX,TY)
   	<-  .wait({+pos(_,_,_)}).

+!search_cow
	: jia.found_cow & scheme_group(Sch,G) &
	  pos(X,Y,ActionId) & not target(TX,TY)
	<-  //.print("Cow found!");
		jmoise.set_goal_state(Sch,search_cow,satisfied);
		jia.near_least_visited(X,Y,ToX,ToY);
		!pos(ToX,ToY).

+!search_cow. //<- .print("[search_cow] do nothing").

-!search_cow[error_msg(M),code(C),code_line(L)]
	<- .print("Error on search_cow, command: ",C,", line ",L,", message: ",M).

{ end }
*/