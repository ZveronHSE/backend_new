CREATE USER postgres SUPERUSER;
CREATE EXTENSION IF NOT EXISTS dblink;

DO
$$
    BEGIN
        PERFORM dblink_exec('', 'CREATE DATABASE address');
    EXCEPTION
        WHEN duplicate_database THEN RAISE NOTICE '%, skipping', SQLERRM USING ERRCODE = SQLSTATE;
    END
$$;

DO
$$
    BEGIN
        PERFORM dblink_exec('', 'CREATE DATABASE parameter');
    EXCEPTION
        WHEN duplicate_database THEN RAISE NOTICE '%, skipping', SQLERRM USING ERRCODE = SQLSTATE;
    END
$$;

DO
$$
    BEGIN
        PERFORM dblink_exec('', 'CREATE DATABASE blacklist');
    EXCEPTION
        WHEN duplicate_database THEN RAISE NOTICE '%, skipping', SQLERRM USING ERRCODE = SQLSTATE;
    END
$$;

DO
$$
BEGIN
        PERFORM dblink_exec('', 'CREATE DATABASE favorites');
EXCEPTION
        WHEN duplicate_database THEN RAISE NOTICE '%, skipping', SQLERRM USING ERRCODE = SQLSTATE;
END
$$;

DO
$$
BEGIN
        PERFORM dblink_exec('', 'CREATE DATABASE profile');
EXCEPTION
        WHEN duplicate_database THEN RAISE NOTICE '%, skipping', SQLERRM USING ERRCODE = SQLSTATE;
END
$$;

DO
$$
    BEGIN
        PERFORM dblink_exec('', 'CREATE DATABASE auth');
    EXCEPTION
        WHEN duplicate_database THEN RAISE NOTICE '%, skipping', SQLERRM USING ERRCODE = SQLSTATE;
    END
$$;

DO
$$
    BEGIN
        PERFORM dblink_exec('', 'CREATE DATABASE apigateway');
    EXCEPTION
        WHEN duplicate_database THEN RAISE NOTICE '%, skipping', SQLERRM USING ERRCODE = SQLSTATE;
    END
$$;

-- сюда добавлять, если нужны новые БД

DROP USER postgres;