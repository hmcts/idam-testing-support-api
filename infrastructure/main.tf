terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.4.0"
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

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-USER"
  value        = module.idam-testing-support-api-db-v14[0].username
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-PASS"
  value        = module.idam-testing-support-api-db-v14[0].password
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-HOST"
  value        = module.idam-testing-support-api-db-v14[0].fqdn
  key_vault_id = data.azurerm_key_vault.default.id
}

# These two are not exported by the v14 module, but used by the pods
resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-PORT"
  value        = "5432"
  key_vault_id = data.azurerm_key_vault.default.id
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  count        = local.instance_count
  name         = "${local.default_name}-POSTGRES-DATABASE"
  value        = var.database_name
  key_vault_id = data.azurerm_key_vault.default.id
}
