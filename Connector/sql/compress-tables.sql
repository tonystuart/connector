connect 'jdbc:derby://localhost:1527/Repository';

call SYSCS_UTIL.SYSCS_COMPRESS_TABLE('APP', 'PROPERTY', 1);
call SYSCS_UTIL.SYSCS_COMPRESS_TABLE('APP', 'ENTITY', 1);
call SYSCS_UTIL.SYSCS_COMPRESS_TABLE('APP', 'AUTHORITY', 1);
