/*
 * Copyright 2021-2021 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.nlelection.common

import eu.cdevreeze.yaidom2.core.EName

/**
 * Common namespaces and ENames.
 *
 * @author Chris de Vreeze
 */
object ENames {

  val EmlNs = "urn:oasis:names:tc:evs:schema:eml"
  val KrNs = "http://www.kiesraad.nl/extensions"
  val XalNs = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0"
  val XnlNs = "urn:oasis:names:tc:ciq:xsdschema:xNL:2.0"

  val IdEName: EName = EName.fromLocalName("Id")

  val CommitteeCategoryEName: EName = EName.fromLocalName("CommitteeCategory")
  val CommitteeNameEName: EName = EName.fromLocalName("CommitteeName")
  val NameTypeEName: EName = EName.fromLocalName("NameType")
  val ReasonCodeEName: EName = EName.fromLocalName("ReasonCode")
  val RegionCategoryEName: EName = EName.fromLocalName("RegionCategory")
  val RegionNumberEName: EName = EName.fromLocalName("RegionNumber")
  val ShortCodeEName: EName = EName.fromLocalName("ShortCode")
  val SuperiorRegionCategoryEName: EName = EName.fromLocalName("SuperiorRegionCategory")
  val SuperiorRegionNumberEName: EName = EName.fromLocalName("SuperiorRegionNumber")

  val EmlAffiliationEName: EName = EName(EmlNs, "Affiliation")
  val EmlAffiliationIdentifierEName: EName = EName(EmlNs, "AffiliationIdentifier")
  val EmlCandidateEName: EName = EName(EmlNs, "Candidate")
  val EmlCandidateFullNameEName: EName = EName(EmlNs, "CandidateFullName")
  val EmlCandidateIdentifierEName: EName = EName(EmlNs, "CandidateIdentifier")
  val EmlCandidateListEName: EName = EName(EmlNs, "CandidateList")
  val EmlCastEName: EName = EName(EmlNs, "Cast")
  val EmlContestEName: EName = EName(EmlNs, "Contest")
  val EmlContestIdentifierEName: EName = EName(EmlNs, "ContestIdentifier")
  val EmlContestNameEName: EName = EName(EmlNs, "ContestName")
  val EmlContestsEName: EName = EName(EmlNs, "Contests")
  val EmlCountEName: EName = EName(EmlNs, "Count")
  val EmlElectedEName: EName = EName(EmlNs, "Elected")
  val EmlElectionEName: EName = EName(EmlNs, "Election")
  val EmlElectionCategoryEName: EName = EName(EmlNs, "ElectionCategory")
  val EmlElectionEventEName: EName = EName(EmlNs, "ElectionEvent")
  val EmlElectionNameEName: EName = EName(EmlNs, "ElectionName")
  val EmlElectionIdentifierEName: EName = EName(EmlNs, "ElectionIdentifier")
  val EmlGenderEName: EName = EName(EmlNs, "Gender")
  val EmlQualifyingAddressEName: EName = EName(EmlNs, "QualifyingAddress")
  val EmlRankingEName: EName = EName(EmlNs, "Ranking")
  val EmlRegisteredNameEName: EName = EName(EmlNs, "RegisteredName")
  val EmlRejectedVotesEName: EName = EName(EmlNs, "RejectedVotes")
  val EmlReportingUnitIdentifierEName: EName = EName(EmlNs, "ReportingUnitIdentifier")
  val EmlReportingUnitVotesEName: EName = EName(EmlNs, "ReportingUnitVotes")
  val EmlResultEName: EName = EName(EmlNs, "Result")
  val EmlSelectionEName: EName = EName(EmlNs, "Selection")
  val EmlTotalCountedEName: EName = EName(EmlNs, "TotalCounted")
  val EmlTotalVotesEName: EName = EName(EmlNs, "TotalVotes")
  val EmlUncountedVotesEName: EName = EName(EmlNs, "UncountedVotes")
  val EmlValidVotesEName: EName = EName(EmlNs, "ValidVotes")

  val KrCommitteeEName: EName = EName(KrNs, "Committee")
  val KrElectionDateEName: EName = EName(KrNs, "ElectionDate")
  val KrElectionSubcategoryEName: EName = EName(KrNs, "ElectionSubcategory")
  val KrElectionTreeEName: EName = EName(KrNs, "ElectionTree")
  val KrNominationDateEName: EName = EName(KrNs, "NominationDate")
  val KrRegionEName: EName = EName(KrNs, "Region")
  val KrRegionNameEName: EName = EName(KrNs, "RegionName")
  val KrRegisteredAppellationEName: EName = EName(KrNs, "RegisteredAppellation")
  val KrRegisteredPartyEName: EName = EName(KrNs, "RegisteredParty")

  val XalLocalityEName: EName = EName(XalNs, "Locality")
  val XalLocalityNameEName: EName = EName(XalNs, "LocalityName")

  val XnlPersonNameEName: EName = EName(XnlNs, "PersonName")
  val XnlNameLineEName: EName = EName(XnlNs, "NameLine")
  val XnlFirstNameEName: EName = EName(XnlNs, "FirstName")
  val XnlNamePrefixEName: EName = EName(XnlNs, "NamePrefix")
  val XnlLastNameEName: EName = EName(XnlNs, "LastName")
}
