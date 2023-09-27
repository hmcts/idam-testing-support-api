terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.74.0"
    }
  }
}

provider "azurerm" {
  features {}
}

provider "azurerm" {
  subscription_id            = local.cft_vnet[local.env].subscription
  skip_provider_registration = "true"
  features {}
  alias = "cft_vnet"
}

locals {
  default_name = "${var.product}-${var.component}"
  vault_name   = "${var.product}-${var.env}"
  instance_count = (var.env == "prod" || var.env == "idam-prod" || var.env == "idam-prod2") ? 0 : 1
  environments = {
    "idam-prod"     = "production",
    "idam-aat"      = "staging",
    "idam-perftest" = "testing",
    "idam-ithc"     = "testing",
    "idam-demo"     = "demo",
    "idam-preview"  = "development",
    "idam-sandbox"  = "sandbox"
  }
  tags = merge(
    var.common_tags,
    {
      "environment"         = lookup(local.environments, var.env)
    },
  )

  env_temp               = replace(var.env,"idam-","")
  env                    = local.env_temp == "sandbox" ? "sbox" : local.env_temp
  cft_vnet = {
    sbox = {
      subscription = "b72ab7b7-723f-4b18-b6f6-03b0f2c6a1bb"
    }
    perftest = {
      subscription = "8a07fdcd-6abd-48b3-ad88-ff737a4b9e3c"
    }
    aat = {
      subscription = "96c274ce-846d-4e48-89a7-d528432298a7"
    }
    ithc = {
      subscription = "62864d44-5da9-4ae9-89e7-0cf33942fa09"
    }
    preview = {
      subscription = "8b6ea922-0862-443e-af15-6056e1c9b9a4"
    }
    demo = {
      subscription = "d025fece-ce99-4df2-b7a9-b649d3ff2060"
    }
    prod = {
      subscription = "8cbc6f36-7c56-4963-9d36-739db5d00b27"
    }
  }
}

module "idam-testing-support-api-db" {
  count              = local.instance_count
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = local.default_name
  location           = var.location
  env                = var.env
  subscription       = var.subscription
  postgresql_user    = "idamtstsptapi"
  database_name      = "idamtstsptapi"
  postgresql_version = 11
  sku_name           = "GP_Gen5_4"
  sku_tier           = "GeneralPurpose"
  sku_capacity       = "4"
  storage_mb         = "51200"
  common_tags        = local.tags
}

module "idam-testing-support-api-db-v14" {
  count              = local.instance_count

  providers = {
    azurerm.postgres_network = azurerm.cft_vnet
  }

  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  env    = var.env

  product              = var.product
  component            = var.component
  business_area        = "cft"
  common_tags          = local.tags
  name                 = "${var.product}-${var.env}-v14-testing-support-api"

  pgsql_databases = [
    {
      name : "idamtstsptapi"
    }
  ]

  pgsql_version = "14"

  admin_user_object_id = var.jenkins_AAD_objectId
}

data "azurerm_key_vault" "default" {
  name                = local.vault_name
  resource_group_name = "${var.product}-${var.env}"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-USER"
  value        = module.idam-testing-support-api-db[0].user_name
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-PASS"
  value        = module.idam-testing-support-api-db[0].postgresql_password
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-HOST"
  value        = module.idam-testing-support-api-db[0].host_name
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-PORT"
  value        = module.idam-testing-support-api-db[0].postgresql_listen_port
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-DATABASE"
  value        = module.idam-testing-support-api-db[0].postgresql_database
  key_vault_id = data.azurerm_key_vault.default.id
}

# TEMP for v14
resource "azurerm_key_vault_secret" "POSTGRES-USER_V14" {
  name         = "${local.default_name}-POSTGRES-USER-v14"
  value        = module.idam-testing-support-api-db-v14.username
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS_V14" {
  name         = "${local.default_name}-POSTGRES-PASS-v14"
  value        = module.idam-testing-support-api-db-v14.password
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST_V14" {
  name         = "${local.default_name}-POSTGRES-HOST-v14"
  value        = module.idam-testing-support-api-db-v14.fqdn
  key_vault_id = data.azurerm_key_vault.default.id
}
