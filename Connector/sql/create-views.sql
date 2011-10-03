connect 'jdbc:derby://localhost:1527/Repository';

drop view ev;

create view ev 
as
select
  th(id) as id,
  type,
  name,
  th(parent_entity_id) as parent_entity_id,
  th(created_by) as created_by,
  d(created_at) as created_at,
  d(published_at) as published_at
from entity
;

drop view pv;

create view pv
as
select
  th(entity_id) as entity_id,
  substr(property_name, 1, 32) as property_name,
  index,
  coalesce
  (
  	th(entity_value),
	char(integer_value),
	char(bigint_value),
	char(boolean_value),
	substr(ra(varchar_value, '\s+', ' '), 1, 32),
	char(utc_millis_value),
	substr(enum_value, 1, 32)
  ) as value,
  d(valid_from) as valid_from_date,
  d(valid_to) as valid_to_date,
  th(modified_by) as modified_by
from property
order by valid_from, entity_id, property_name, index
;

drop view av;

create view av
as
select
  th(workflow_id) as workflow_id,
  th(content_id) as content_id,
  th(granted_to_id) as granted_to_id,
  granted_to_type,
  access_type
from authority
;