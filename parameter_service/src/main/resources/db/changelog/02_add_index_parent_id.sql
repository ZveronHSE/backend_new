-- changeset WolfAlm:1672868866000-2
create index category_id_parent_index
    on category (id_parent);