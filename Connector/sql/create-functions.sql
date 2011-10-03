connect 'jdbc:derby://localhost:1527/Repository';

drop function d;
drop function fh;
drop function th;
drop function ra;

create function d
        ( value bigint )
        returns varchar(22)
        language java
        deterministic
        external name 'com.semanticexpression.connector.server.DerbyUtils.d'
        parameter style java
        no sql
        returns null on null input
;

create function fh
        ( buf varchar(19) )
        returns bigint
        language java
        deterministic
        external name 'com.semanticexpression.connector.server.DerbyUtils.fh'
        parameter style java
        no sql
        returns null on null input
;

create function th
        ( value bigint )
        returns varchar(19)
        language java
        deterministic
        external name 'com.semanticexpression.connector.server.DerbyUtils.th'
        parameter style java
        no sql
        returns null on null input
;

create function ra
        ( string varchar(1000) , regular_expression varchar(1000), replacement varchar(1000) )
        returns varchar(1000)
        language java
        deterministic
        external name 'com.semanticexpression.connector.server.DerbyUtils.ra'
        parameter style java
        no sql
        returns null on null input
;
    