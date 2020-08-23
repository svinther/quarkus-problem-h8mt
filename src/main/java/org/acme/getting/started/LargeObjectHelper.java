package org.acme.getting.started;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class LargeObjectHelper {

    private Logger log = Logger.getLogger(LargeObjectHelper.class);

    private Connection con;

    public LargeObjectHelper(Connection con) {
        this.con = con;
    }

    private LargeObjectManager getLargeObjectManager() {
        try {
            PgConnection pgConnection = con.unwrap(PgConnection.class);
            return pgConnection.getLargeObjectAPI();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * Get a blob from the database with the given oid.
     * Don't forget to close() the blob
     * @param loOID oid identifier of the blob
     */
    public LargeObject getInputBlob(Long loOID) {
        try {
            LargeObjectManager largeObjectAPI = getLargeObjectManager();
            return largeObjectAPI.open(loOID, LargeObjectManager.READ);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * Get a blob from the database with the given oid.
     * Currently, the blob is copied to memory before streamed.
     * @param loOid oid identifier of the blob
     */
    public byte[] getBlobAsByteArray(Long loOid) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (LargeObject largeObject = getInputBlob(loOid); InputStream inputStream = largeObject.getInputStream()) {
            IOUtils.copy(inputStream, buffer);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
        return buffer.toByteArray();
    }

    /***
     * Creates a blob from an inputstream.
     * Make sure the inputstream has not been closed or consumed.
     * @param inputStream Stream of bytes.
     * @return oid of large object
     */
    public long createBlob(InputStream inputStream) {
        try {
            LargeObjectManager lom = getLargeObjectManager();
            long oid = lom.createLO();
            try (LargeObject lob = lom.open(oid, LargeObjectManager.WRITE); OutputStream outputStream = lob.getOutputStream()) {
                IOUtils.copy(inputStream, outputStream);
                int size = lob.size();
                return oid;
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /***
     * Deletes a blob with the given oid.
     * @param loOID oid identifier of the blob
     */
    public void deleteBlob(long loOID) throws SQLException {
        LargeObjectManager lom = getLargeObjectManager();
        lom.delete(loOID);
    }
}
