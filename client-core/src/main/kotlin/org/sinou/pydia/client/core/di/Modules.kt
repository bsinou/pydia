package org.sinou.pydia.client.core.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.sinou.pydia.client.core.db.accounts.AccountDB
import org.sinou.pydia.client.core.db.auth.AuthDB
import org.sinou.pydia.client.core.db.preferences.CELLS_PREFERENCES_NAME
import org.sinou.pydia.client.core.db.preferences.legacyMigrations
import org.sinou.pydia.client.core.db.runtime.RuntimeDB
import org.sinou.pydia.client.core.services.AccountService
import org.sinou.pydia.client.core.services.AppCredentialService
import org.sinou.pydia.client.core.services.AuthService
import org.sinou.pydia.client.core.services.ConnectionService
import org.sinou.pydia.client.core.services.CoroutineService
import org.sinou.pydia.client.core.services.ErrorService
import org.sinou.pydia.client.core.services.FileService
import org.sinou.pydia.client.core.services.JobService
import org.sinou.pydia.client.core.services.NetworkService
import org.sinou.pydia.client.core.services.NodeService
import org.sinou.pydia.client.core.services.OfflineService
import org.sinou.pydia.client.core.services.PasswordStore
import org.sinou.pydia.client.core.services.PreferencesService
import org.sinou.pydia.client.core.services.SessionFactory
import org.sinou.pydia.client.core.services.TokenStore
import org.sinou.pydia.client.core.services.TransferService
import org.sinou.pydia.client.core.services.TreeNodeRepository
import org.sinou.pydia.client.core.services.WorkerService
import org.sinou.pydia.client.core.services.workers.OfflineSyncWorker
import org.sinou.pydia.client.ui.account.AccountListVM
import org.sinou.pydia.client.ui.browse.models.AccountHomeVM
import org.sinou.pydia.client.ui.browse.models.BookmarksVM
import org.sinou.pydia.client.ui.browse.models.CarouselVM
import org.sinou.pydia.client.ui.browse.models.FilterTransferByMenuVM
import org.sinou.pydia.client.ui.browse.models.FolderVM
import org.sinou.pydia.client.ui.browse.models.NodeActionsVM
import org.sinou.pydia.client.ui.browse.models.OfflineVM
import org.sinou.pydia.client.ui.browse.models.SingleTransferVM
import org.sinou.pydia.client.ui.browse.models.SortByMenuVM
import org.sinou.pydia.client.ui.browse.models.TransfersVM
import org.sinou.pydia.client.ui.browse.models.TreeNodeVM
import org.sinou.pydia.client.ui.login.models.LoginVM
import org.sinou.pydia.client.ui.login.models.OAuthVM
import org.sinou.pydia.client.ui.models.BrowseRemoteVM
import org.sinou.pydia.client.ui.system.models.HouseKeepingVM
import org.sinou.pydia.client.ui.system.models.JobListVM
import org.sinou.pydia.client.ui.system.models.LandingVM
import org.sinou.pydia.client.ui.system.models.LogListVM
import org.sinou.pydia.client.ui.system.models.PrefReadOnlyVM
import org.sinou.pydia.client.ui.system.models.SettingsVM
import org.sinou.pydia.sdk.api.Server
import org.sinou.pydia.sdk.api.Store
import org.sinou.pydia.sdk.api.Transport
import org.sinou.pydia.sdk.transport.auth.Token
import org.sinou.pydia.sdk.utils.MemoryStore

// Well Known IDs
class DiNames {
    companion object {
        // Dispatchers
        const val uiDispatcher = "uiDispatcher"
        const val ioDispatcher = "ioDispatcher"
        const val cpuDispatcher = "cpuDispatcher"

        // Stores
        const val tokenStore = "TokenStore"
        const val passwordStore = "PasswordStore"
        const val serverStore = "ServerStore"
        const val transportStore = "TransportStore"
    }
}

// Databases are only referenced locally
private const val RUNTIME_DB_NAME = "runtimedb"
private const val AUTH_DB_NAME = "authdb"
private const val ACCOUNT_DB_NAME = "accountdb"

val appModule = module {
    single {
        PreferenceDataStoreFactory.create(
            migrations = legacyMigrations(androidContext().applicationContext)
        ) {
            androidContext().applicationContext.preferencesDataStoreFile(CELLS_PREFERENCES_NAME)
        }
    }

    singleOf(::PreferencesService)
}

val dbModule = module {

    // Runtime DB
    single {
        Room.databaseBuilder(
            androidContext().applicationContext,
            RuntimeDB::class.java,
            RUNTIME_DB_NAME
        )
//            .addMigrations(RuntimeDB.MIGRATION_1_2)
//            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    // Auth DB
    single {
        Room.databaseBuilder(
            androidContext().applicationContext,
            AuthDB::class.java,
            AUTH_DB_NAME
        )
            .fallbackToDestructiveMigrationFrom(1)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    // Account DB
    single {
        Room.databaseBuilder(
            androidContext().applicationContext,
            AccountDB::class.java,
            ACCOUNT_DB_NAME
        )
            .fallbackToDestructiveMigrationFrom(1)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }
}

val daoModule = module {

    single { get<RuntimeDB>().jobDao() }
    single { get<RuntimeDB>().logDao() }

    single { get<AuthDB>().tokenDao() }
    single { get<AuthDB>().legacyCredentialsDao() }
    single { get<AuthDB>().authStateDao() }

    single { get<AccountDB>().accountDao() }
    single { get<AccountDB>().sessionViewDao() }
    single { get<AccountDB>().sessionDao() }
    single { get<AccountDB>().workspaceDao() }
}

val serviceModule = module {

    // Enable better management of context and testing
    single(named(DiNames.uiDispatcher)) {
        Dispatchers.Main
    }
    single(named(DiNames.ioDispatcher)) {
        Dispatchers.IO
    }
    single(named(DiNames.cpuDispatcher)) {
        Dispatchers.Default
    }

    single {
        CoroutineService(
            get(named(DiNames.uiDispatcher)),
            get(named(DiNames.ioDispatcher)),
            get(named(DiNames.cpuDispatcher)),
        )
    }

    single { ErrorService(get()) }

    single {
        WorkerService(
            androidContext(),
            get(),
            get(),
            get(),
        )
    }

    // Network state
    single { NetworkService(androidContext(), get()) }

    // Long running jobs
    single { JobService(get(), get()) }

    // Authentication
    single<Store<Token>>(named(DiNames.tokenStore)) { TokenStore(get()) }
    single<Store<String>>(named(DiNames.passwordStore)) { PasswordStore(get()) }

    single<Store<Server>>(named(DiNames.serverStore)) { MemoryStore() }
    single<Store<Transport>>(named(DiNames.transportStore)) { MemoryStore() }

    single {
        AppCredentialService(
            get(named(DiNames.tokenStore)),
            get(named(DiNames.passwordStore)),
            get(named(DiNames.transportStore)),
            get(), // CoroutineService
            get(), // NetworkService
            get(), // AccountDao
            get(), // SessionDao
            get(), // SessionViewDao
        )
    }
    single { AuthService(get(), get()) }
    single { FileService(androidContext().applicationContext, get(), get()) }

    // Accounts
    single {
        TreeNodeRepository(
            androidContext().applicationContext,
            get(),
            get()
        )
    }

    // Sessions
    single {
        SessionFactory(
            get(),
            get(),
            get(named(DiNames.serverStore)),
            get(named(DiNames.transportStore)),
            get()
        )
    }
    single { AccountService(get(), get(), get(), get(), get(), get(), get()) }

    // Business services
    single { NodeService(androidContext().applicationContext, get(), get(), get(), get(), get()) }
    single { ConnectionService(get(), get(), get(), get(), get()) }

    single {
        OfflineService(
            androidContext().applicationContext,
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    single {
        TransferService(
            androidContext().applicationContext,
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }


    worker { (workerParams: WorkerParameters) ->
        OfflineSyncWorker(
            appContext = get(),
            params = workerParams,
        )
    }
}

val viewModelModule = module {

    viewModelOf(::LandingVM)

    viewModelOf(::SettingsVM)
    viewModelOf(::PrefReadOnlyVM)

    viewModelOf(::OAuthVM)
    viewModelOf(::LoginVM)

    viewModelOf(::BrowseRemoteVM)

    viewModelOf(::AccountListVM)
    viewModelOf(::AccountHomeVM)
    viewModelOf(::HouseKeepingVM)

    viewModelOf(::SortByMenuVM)
    viewModelOf(::FilterTransferByMenuVM)

    viewModelOf(::FolderVM)
    viewModelOf(::TreeNodeVM)
    viewModelOf(::NodeActionsVM)

    viewModelOf(::CarouselVM)
    viewModelOf(::BookmarksVM)
    viewModelOf(::OfflineVM)

    viewModelOf(::TransfersVM)
    viewModelOf(::SingleTransferVM)

    viewModelOf(::JobListVM)
    viewModelOf(::LogListVM)
}

val allModules = appModule + dbModule + daoModule + serviceModule + viewModelModule
