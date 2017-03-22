package org.opencb.cellbase.server.ws.clinical;

import io.swagger.annotations.*;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.ClinicalDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

/**
 * Created by fjlopez on 06/12/16.
 */
@Path("/{version}/{species}/clinical")
@Produces("application/json")
@Api(value = "Clinical", description = "Clinical RESTful Web Services API")
public class ClinicalWSServer extends GenericRestWSServer {


    public ClinicalWSServer(@PathParam("version")
                                  @ApiParam(name = "version", value = "Possible values: v3, v4",
                                          defaultValue = "v4") String version,
                                  @PathParam("species")
                                  @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                                          + "of potentially available species ids, please refer to: "
                                          + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/meta/species") String species,
                                  @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/variant/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time. ",
            value = "Retrieves all clinical variants", response = Variant.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "source",
                    value = "Comma separated list of database sources of the documents to be returned. Possible values "
                            + " are clinvar,cosmic or gwas. E.g.: clinvar,cosmic",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "so",
                    value = "Comma separated list of sequence ontology term names, e.g.: missense_variant. Exact text "
                            + "matches will be returned.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "feature",
                    value = "Comma separated list of feature ids, which can be either ENSEMBL gene ids, HGNC gene symbols,"
                            + " transcript symbols or ENSEMBL transcript ids, e.g.: BRCA2, ENST00000409047. Exact text"
                            + " matches will be returned.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "phenotypeDisease",
                    value = "String to indicate the phenotypes/diseases to query. A text search will be run.",
                    required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "accession",
                    value = "Comma separated list of database accesions, e.g.: RCV000033215,COSM306824",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "id",
                    value = "Comma separated list of ids, e.g.: rs6025",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "type",
                    value = "Comma separated list of variant types, e.g. \"SNV\" ",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "reviewStatus",
                    value = "Comma separated list of review labels, e.g.: CRITERIA_PROVIDED_SINGLE_SUBMITTER",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "clinicalSignificance",
                    value = "Comma separated list of clinical significance labels, e.g.: Benign",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getAll() {
        try {
            parseQueryParams();
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory2.getClinicalDBAdaptor(this.species, this.assembly);

            return createOkResponse(clinicalDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

}