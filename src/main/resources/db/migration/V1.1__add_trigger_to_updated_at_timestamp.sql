CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    NEW.updated_at := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

CREATE TRIGGER therapists_set_updated_at
    BEFORE UPDATE ON therapists
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();