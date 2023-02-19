-- changeset WolfAlm:1672868866023-1
alter table public.lot_form
    rename column form to id_category;

-- changeset WolfAlm:1672868866023-2
alter table public.lot_form
    alter column id_category type integer using id_category::integer;

