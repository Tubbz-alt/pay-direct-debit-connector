package uk.gov.pay.directdebit.events.dao;

import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import uk.gov.pay.directdebit.events.dao.mapper.GoCardlessEventMapper;
import uk.gov.pay.directdebit.events.model.GoCardlessOrganisationIdArgumentFactory;
import uk.gov.pay.directdebit.gatewayaccounts.model.GoCardlessOrganisationId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateId;
import uk.gov.pay.directdebit.mandate.model.GoCardlessMandateIdArgumentFactory;
import uk.gov.pay.directdebit.payments.model.GoCardlessEvent;
import uk.gov.pay.directdebit.payments.model.GoCardlessEventIdArgumentFactory;
import uk.gov.pay.directdebit.payments.model.GoCardlessPaymentIdArgumentFactory;

import java.util.Optional;
import java.util.Set;

@RegisterArgumentFactory(GoCardlessEventIdArgumentFactory.class)
@RegisterArgumentFactory(GoCardlessMandateIdArgumentFactory.class)
@RegisterArgumentFactory(GoCardlessPaymentIdArgumentFactory.class)
@RegisterArgumentFactory(GoCardlessOrganisationIdArgumentFactory.class)
@RegisterRowMapper(GoCardlessEventMapper.class)
public interface GoCardlessEventDao {

    @SqlUpdate("INSERT INTO gocardless_events(" +
            " event_id," +
            " action," +
            " resource_type," +
            " json," +
            " details_cause," +
            " details_description," +
            " details_origin," +
            " details_reason_code," +
            " details_scheme," +
            " links_mandate," +
            " links_new_customer_bank_account," +
            " links_new_mandate," +
            " links_organisation," +
            " links_parent_event," +
            " links_payment," +
            " links_payout," +
            " links_previous_customer_bank_account," +
            " links_refund," +
            " links_subscription," +
            " created_at)" +
            " VALUES (" +
            " :goCardlessEventId," +
            " :action," +
            " :resourceType," +
            " CAST(:json as jsonb)," +
            " :detailsCause," +
            " :detailsDescription," +
            " :detailsOrigin," +
            " :detailsReasonCode," +
            " :detailsScheme," +
            " :linksMandate," +
            " :linksNewCustomerBankAccount," +
            " :linksNewMandate," +
            " :linksOrganisation," +
            " :linksParentEvent," +
            " :linksPayment," +
            " :linksPayout," +
            " :linksPreviousCustomerBankAccount," +
            " :linksRefund," +
            " :linksSubscription," +
            " :createdAt)")
    @GetGeneratedKeys
    Long insert(@BindBean GoCardlessEvent goCardlessEvent);

    @SqlUpdate("UPDATE gocardless_events SET internal_event_id = :eventId WHERE id = :id")
    int updateEventId(@Bind("id") Long id, @Bind("eventId") Long eventId);
    
    @SqlQuery("SELECT id, " +
            "internal_event_id, " +
            "event_id, " +
            "action, " +
            "created_at, " +
            "details_cause, " +
            "details_description, " +
            "details_origin, " +
            "details_reason_code, " +
            "details_scheme, " +
            "resource_type," +
            "links_mandate, " +
            "links_new_customer_bank_account, " +
            "links_new_mandate, " +
            "links_organisation, " +
            "links_parent_event, " +
            "links_payment, " +
            "links_payout, " +
            "links_previous_customer_bank_account, " +
            "links_refund, " +
            "links_subscription, " +
            "json, " +
            "event_id " +
            "FROM gocardless_events " +
            "WHERE links_mandate = :linksMandate " +
            "AND links_organisation = :linksOrganisation " +
            "AND action IN (<applicableActions>) " +
            "ORDER BY created_at DESC " +
            "LIMIT 1")
    Optional<GoCardlessEvent> findLatestApplicableEventForMandate(@Bind("linksMandate") GoCardlessMandateId goCardlessMandateId,
                                                                        @Bind("linksOrganisation") GoCardlessOrganisationId goCardlessOrganisationId,
                                                                        @BindList("applicableActions") Set<String> applicableActions);

}
